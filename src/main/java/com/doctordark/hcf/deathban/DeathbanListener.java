package com.doctordark.hcf.deathban;

import com.doctordark.hcf.ConfigurationService;
import com.doctordark.hcf.DelayedMessageRunnable;
import com.doctordark.hcf.DurationFormatter;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import net.minecraft.util.gnu.trove.map.TObjectIntMap;
import net.minecraft.util.gnu.trove.map.TObjectLongMap;
import net.minecraft.util.gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.gnu.trove.map.hash.TObjectLongHashMap;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRequestRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeathbanListener implements Listener {

    private static final long RESPAWN_KICK_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(10L);
    private static final long RESPAWN_KICK_DELAY_TICKS = RESPAWN_KICK_DELAY_MILLIS / 50L;
    private static final long LIFE_USE_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    private static final String LIFE_USE_DELAY_WORDS = DurationFormatUtils.formatDurationWords(DeathbanListener.LIFE_USE_DELAY_MILLIS, true, true);
    private static final String DEATH_BAN_BYPASS_PERMISSION = "hcf.deathban.bypass";

    private final TObjectIntMap<UUID> respawnTickTasks = new TObjectIntHashMap<>();
    private final TObjectLongMap<UUID> lastAttemptedJoinMap = new TObjectLongHashMap<>();
    private final HCF plugin;

    public DeathbanListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(DeathbanListener.DEATH_BAN_BYPASS_PERMISSION)) {
            return;
        }

        FactionUser user = this.plugin.getUserManager().getUser(player.getUniqueId());
        Deathban deathban = user.getDeathban();
        if (deathban == null || !deathban.isActive()) {
            return;
        }

        if (this.plugin.getEotwHandler().isEndOfTheWorld()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Deathbanned for the entirety of the map due to EOTW.\nCome back tomorrow for SOTW.");
            return;
        }

        UUID uuid = player.getUniqueId();
        int lives = this.plugin.getDeathbanManager().getLives(uuid);

        String formattedRemaining = DurationFormatter.getRemaining(deathban.getRemaining(), true, false);

        if (lives <= 0) {  // If the user has no lives, inform that they need some.
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    ChatColor.RED + "You are still deathbanned for " + formattedRemaining + ": " + ChatColor.YELLOW + deathban.getReason() + ChatColor.RED + ".\n" +
                            "You can purchase lives at " + ConfigurationService.DONATE_URL + " to bypass this.");

            return;
        }

        long millis = System.currentTimeMillis();
        long lastAttemptedJoinMillis = this.lastAttemptedJoinMap.get(uuid);

        // If the user has tried joining in the last 30 seconds and kicked for deathban but has lives, let them join this time to prevent accidental life use.
        if (lastAttemptedJoinMillis != this.lastAttemptedJoinMap.getNoEntryValue() && lastAttemptedJoinMillis - millis < DeathbanListener.LIFE_USE_DELAY_MILLIS) {
            this.lastAttemptedJoinMap.remove(uuid);
            user.removeDeathban();
            lives = plugin.getDeathbanManager().takeLives(uuid, 1);

            event.setResult(PlayerLoginEvent.Result.ALLOWED);
            new DelayedMessageRunnable(player, ChatColor.YELLOW + "You have used a life for entry. You now have " + ChatColor.WHITE + lives + ChatColor.YELLOW + " lives.").
                    runTask(plugin);

            return;
        }

        // The user has lives, but just in case they didn't want them to use, tell them to join again in the next 30 seconds.
        String reason = deathban.getReason();
        this.lastAttemptedJoinMap.put(uuid, millis + LIFE_USE_DELAY_MILLIS);

        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You are currently death-banned" + (reason != null ? " for " + reason : "") + ".\n" +
                ChatColor.WHITE + formattedRemaining + " remaining.\n" +
                ChatColor.RED + "You currently have " + (lives <= 0 ? "no" : lives) + " lives.\n\n" +
                "You may use a life by reconnecting within " + ChatColor.YELLOW + DeathbanListener.LIFE_USE_DELAY_WORDS + ChatColor.RED + ".");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Deathban deathban = plugin.getDeathbanManager().applyDeathBan(player, event.getDeathMessage());
        long remaining = deathban.getRemaining();
        if (remaining <= 0L || player.hasPermission(DeathbanListener.DEATH_BAN_BYPASS_PERMISSION)) {
            return;
        }

        if (DeathbanListener.RESPAWN_KICK_DELAY_MILLIS <= 0L || remaining < DeathbanListener.RESPAWN_KICK_DELAY_MILLIS) {
            this.handleKick(player, deathban);
            return;
        }

        // Let the player see the death screen for 10 seconds
        this.respawnTickTasks.put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                DeathbanListener.this.handleKick(player, deathban);
            }
        }.runTaskLater(plugin, DeathbanListener.RESPAWN_KICK_DELAY_TICKS).getTaskId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRequestRespawn(PlayerRequestRespawnEvent event) {
        Player player = event.getPlayer();
        FactionUser user = this.plugin.getUserManager().getUser(player.getUniqueId());
        Deathban deathban = user.getDeathban();
        if (deathban != null && deathban.getRemaining() > 0L) {
            if (player.hasPermission(DeathbanListener.DEATH_BAN_BYPASS_PERMISSION)) {
                this.cancelRespawnKickTask(player);
                user.removeDeathban();
                new DelayedMessageRunnable(player, ChatColor.RED + "You would be death-banned for " + deathban.getReason() + ChatColor.RED + ", but you have access to bypass.").
                        runTask(this.plugin);

                return;
            }

            event.setCancelled(true);
            this.handleKick(player, deathban);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.cancelRespawnKickTask(event.getPlayer());
    }

    private void cancelRespawnKickTask(Player player) {
        int taskId = this.respawnTickTasks.remove(player.getUniqueId());
        if (taskId != this.respawnTickTasks.getNoEntryValue()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private void handleKick(Player player, Deathban deathban) {
        if (this.plugin.getEotwHandler().isEndOfTheWorld()) {
            player.kickPlayer(ChatColor.RED + "Deathbanned for the entirety of the map due to EOTW.\nCome back tomorrow for SOTW!");
        } else {
            player.kickPlayer(ChatColor.RED + "Deathbanned for " + DurationFormatter.getRemaining(deathban.getRemaining(), true, false) + ": " + ChatColor.WHITE + deathban.getReason());
        }
    }
}
