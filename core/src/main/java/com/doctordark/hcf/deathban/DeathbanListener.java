package com.doctordark.hcf.deathban;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import com.doctordark.hcf.util.DelayedMessageRunnable;
import com.doctordark.hcf.util.DurationFormatter;
import net.minecraft.util.gnu.trove.map.TObjectIntMap;
import net.minecraft.util.gnu.trove.map.TObjectLongMap;
import net.minecraft.util.gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.gnu.trove.map.hash.TObjectLongHashMap;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeathbanListener implements Listener {

    private static final long LIFE_USE_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    private static final String LIFE_USE_DELAY_WORDS = DurationFormatUtils.formatDurationWords(DeathbanListener.LIFE_USE_DELAY_MILLIS, true, true);
    private static final String DEATH_BAN_BYPASS_PERMISSION = "hcf.deathban.bypass";

    private final TObjectIntMap<UUID> respawnTickTasks = new TObjectIntHashMap<>();
    private final TObjectLongMap<UUID> lastAttemptedJoinMap = new TObjectLongHashMap<>();
    private final HCF plugin;

    public DeathbanListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().spigot().respawn(); // Method already checks if player is dead first
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        FactionUser user = plugin.getUserManager().getUser(player.getUniqueId());
        Deathban deathban = user.getDeathban();
        if (deathban == null || !deathban.isActive()) {
            return;
        }

        if (player.hasPermission(DeathbanListener.DEATH_BAN_BYPASS_PERMISSION)) {
            plugin.getUserManager().getUser(player.getUniqueId()).removeDeathban();
            informAboutDeathbanBypass(player, deathban, plugin, true);
            return;
        }

        if (deathban.isEotwDeathban()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Deathbanned for the entirety of the map due to EOTW.\nCome back tomorrow for SOTW.");
            return;
        }

        UUID uuid = player.getUniqueId();
        int lives = plugin.getDeathbanManager().getLives(uuid);

        String formattedRemaining = DurationFormatter.getRemaining(deathban.getRemaining(), true, false);
        Location deathbanLocation = deathban.getDeathPoint();

        if (lives <= 0) {  // If the user has no lives, inform that they need some.
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.AQUA + "You have been killed " +
                    ChatColor.GREEN + "(" + ChatColor.WHITE + ChatColor.stripColor(deathban.getReason()) + ChatColor.GREEN + ")" +
                    ChatColor.AQUA + " at " + ChatColor.GOLD + "(" + deathbanLocation.getBlockX() + ", " + deathbanLocation.getBlockY() + ", " + deathbanLocation.getBlockZ() + ")" +
                    ChatColor.AQUA + ". You're deathbanned for " + ChatColor.GREEN + formattedRemaining + ChatColor.AQUA + "."
            );

            return;
        }

        long millis = System.currentTimeMillis();
        long lastAttemptedJoinMillis = lastAttemptedJoinMap.get(uuid);

        // If the user has tried joining in the last 30 seconds and kicked for deathban but has lives, let them join this time to prevent accidental life use.
        if (lastAttemptedJoinMillis != lastAttemptedJoinMap.getNoEntryValue() && lastAttemptedJoinMillis - millis < DeathbanListener.LIFE_USE_DELAY_MILLIS) {
            lastAttemptedJoinMap.remove(uuid);
            user.removeDeathban();
            lives = plugin.getDeathbanManager().takeLives(uuid, 1);

            event.setResult(PlayerLoginEvent.Result.ALLOWED);
            new DelayedMessageRunnable(plugin, player, ChatColor.YELLOW + "You have used a life for entry. You now have " + ChatColor.WHITE + lives + ChatColor.YELLOW + " lives.");

            return;
        }

        // The user has lives, but just in case they didn't want them to use, tell them to join again in the next 30 seconds.
        lastAttemptedJoinMap.put(uuid, millis + LIFE_USE_DELAY_MILLIS);

        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.AQUA + "You have been killed " +
                ChatColor.GREEN + "(" + ChatColor.WHITE + deathban.getReason() + ChatColor.GREEN + ")" +
                ChatColor.AQUA + " at " + ChatColor.GOLD + "(" + deathbanLocation.getBlockX() + ", " + deathbanLocation.getBlockY() + ", " + deathbanLocation.getBlockZ() + ")" +
                ChatColor.AQUA + ". You're deathbanned for " + ChatColor.GREEN + formattedRemaining + ChatColor.AQUA + ". " +
                ChatColor.RED + "You currently have " + lives + " lives.\n\nYou may use one by reconnecting within " +
                ChatColor.YELLOW + DeathbanListener.LIFE_USE_DELAY_WORDS + ChatColor.RED + "."
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Deathban deathban = plugin.getDeathbanManager().applyDeathBan(player, event.getDeathMessage());
        long remaining = deathban.getRemaining();
        if (remaining <= 0L || player.hasPermission(DeathbanListener.DEATH_BAN_BYPASS_PERMISSION)) {
            return;
        }

        long ticks = plugin.getConfiguration().getDeathbanRespawnScreenTicksBeforeKick();

        if (ticks <= 0L || remaining < ticks) {
            handleKick(player, deathban);
            return;
        }

        // Let the player see the death screen for x seconds
        respawnTickTasks.put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                respawnTickTasks.remove(player.getUniqueId());
                handleKick(player, deathban);
            }
        }.runTaskLater(plugin, ticks).getTaskId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRequestRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        FactionUser user = plugin.getUserManager().getUser(player.getUniqueId());
        Deathban deathban = user.getDeathban();
        if (deathban != null && deathban.getRemaining() > 0L) {
            if (player.hasPermission(DeathbanListener.DEATH_BAN_BYPASS_PERMISSION)) {
                cancelRespawnKickTask(player);
                user.removeDeathban();
                informAboutDeathbanBypass(player, deathban, plugin, false);
                return;
            }

            //event.setCancelled(true);
            handleKick(player, deathban);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelRespawnKickTask(event.getPlayer());
    }

    private static void informAboutDeathbanBypass(Player player, Deathban deathban, JavaPlugin plugin, boolean later) {
        String message = ChatColor.RED + "You would be death-banned for " + ChatColor.YELLOW + ChatColor.stripColor(deathban.getReason()) + ChatColor.RED + ", but you have access to bypass.";
        if (later) {
            new DelayedMessageRunnable(plugin, player, message);
        } else {
            player.sendMessage(message);
        }
    }

    private void cancelRespawnKickTask(Player player) {
        int taskId = respawnTickTasks.remove(player.getUniqueId());
        if (taskId != respawnTickTasks.getNoEntryValue()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private void handleKick(Player player, Deathban deathban) {
        if (plugin.getEotwHandler().isEndOfTheWorld()) {
            player.kickPlayer(ChatColor.RED + "Deathbanned for the entirety of the map due to EOTW.\nCome back tomorrow for SOTW!");
        } else {
            player.kickPlayer(ChatColor.RED + "Deathbanned for " + DurationFormatter.getRemaining(deathban.getRemaining(), true, false) + ": " + ChatColor.WHITE + deathban.getReason());
        }
    }
}
