package com.doctordark.hcf.timer.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.event.PlayerClaimEnterEvent;
import com.doctordark.hcf.faction.event.PlayerJoinFactionEvent;
import com.doctordark.hcf.faction.event.PlayerLeaveFactionEvent;
import com.doctordark.hcf.timer.PlayerTimer;
import com.doctordark.hcf.timer.TimerCooldown;
import com.doctordark.hcf.timer.event.TimerStartEvent;
import com.doctordark.hcf.visualise.VisualType;
import com.doctordark.util.BukkitUtils;
import com.doctordark.hcf.util.DurationFormatter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Timer used to tag {@link Player}s in combat to prevent entering safe-zones.
 */
public class CombatTimer extends PlayerTimer implements Listener {

    private final HCF plugin;

    public CombatTimer(HCF plugin) {
        super("Combat", TimeUnit.SECONDS.toMillis(45L));
        this.plugin = plugin;
    }

    @Override
    public String getScoreboardPrefix() {
        return ChatColor.DARK_RED.toString() + ChatColor.BOLD;
    }

    @Override
    public TimerCooldown clearCooldown(@Nullable Player player, UUID playerUUID) {
        TimerCooldown cooldown = super.clearCooldown(player, playerUUID);
        if (cooldown != null && player != null) {
            plugin.getVisualiseHandler().clearVisualBlocks(player, VisualType.SPAWN_BORDER, null);
        }

        return cooldown;
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID playerUUID) {
        super.handleExpiry(player, playerUUID);
        if (player != null) {
            plugin.getVisualiseHandler().clearVisualBlocks(player, VisualType.SPAWN_BORDER, null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionJoin(PlayerJoinFactionEvent event) {
        Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            Player player = optional.get();
            long remaining = getRemaining(player);
            if (remaining > 0L) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot join factions whilst your " + getDisplayName() + ChatColor.RED + " timer is active [" +
                        ChatColor.BOLD + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.RED + " remaining]");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionLeave(PlayerLeaveFactionEvent event) {
        if (event.isForce()) {
            return;
        }

        Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            Player player = optional.get();
            long remaining = getRemaining(player);
            if (remaining > 0L) {
                event.setCancelled(true);

                CommandSender sender = event.getSender();
                if (sender == player) {
                    sender.sendMessage(ChatColor.RED + "Cannot kick " + player.getName() + " as their " + getDisplayName() + ChatColor.RED + " timer is active [" +
                            ChatColor.BOLD + DurationFormatter.getRemaining(remaining, true, false) + ChatColor.RED + " remaining]");
                } else {
                    sender.sendMessage(ChatColor.RED + "You cannot leave factions whilst your " + getDisplayName() + ChatColor.RED + " timer is active [" +
                            ChatColor.BOLD + DurationFormatter.getRemaining(remaining, true, false) + ChatColor.RED + " remaining]");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPreventClaimEnter(PlayerClaimEnterEvent event) {
        if (event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT) {
            return;
        }

        // Prevent entering spawn if the player is spawn tagged.
        Player player = event.getPlayer();
        if (!event.getFromFaction().isSafezone() && event.getToFaction().isSafezone() && getRemaining(player) > 0L) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot enter " + event.getToFaction().getDisplayName(player) +
                    ChatColor.RED + " whilst your " + getDisplayName() + ChatColor.RED + " timer is active [" +
                    ChatColor.BOLD + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.RED + " remaining]");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = BukkitUtils.getFinalAttacker(event, true);
        Entity entity;
        if (attacker != null && (entity = event.getEntity()) instanceof Player) {
            Player attacked = (Player) entity;
            setCooldown(attacker, attacker.getUniqueId(), defaultCooldown, false);
            setCooldown(attacked, attacked.getUniqueId(), defaultCooldown, false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerStart(TimerStartEvent event) {
        if (event.getTimer() == this) {
            Optional<Player> optional = event.getPlayer();
            if (optional.isPresent()) {
                Player player = optional.get();
                player.sendMessage(ChatColor.RED + "You are now in combat.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    @Override
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite, @Nullable Predicate<Long> currentCooldownPredicate) {
        if (player != null && plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone()) {
            return false;
        }
        return super.setCooldown(player, playerUUID, duration, overwrite, currentCooldownPredicate);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPreventClaimEnterMonitor(PlayerClaimEnterEvent event) {
        if (event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT && (!event.getFromFaction().isSafezone() && event.getToFaction().isSafezone())) {
            clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
        }
    }
}
