package org.ipvp.hcf.timer.type;

import com.doctordark.util.BukkitUtils;
import com.google.common.base.Optional;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.ipvp.hcf.DurationFormatter;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.event.PlayerClaimEnterEvent;
import org.ipvp.hcf.faction.event.PlayerJoinFactionEvent;
import org.ipvp.hcf.faction.event.PlayerLeaveFactionEvent;
import org.ipvp.hcf.timer.PlayerTimer;
import org.ipvp.hcf.timer.event.TimerClearEvent;
import org.ipvp.hcf.timer.event.TimerStartEvent;
import org.ipvp.hcf.visualise.VisualType;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerStop(TimerClearEvent event) {
        if (event.getTimer() == this) {
            Optional<UUID> optionalUserUUID = event.getUserUUID();
            if (optionalUserUUID.isPresent()) {
                this.onExpire(optionalUserUUID.get());
            }
        }
    }

    @Override
    public void onExpire(UUID userUUID) {
        Player player = Bukkit.getPlayer(userUUID);
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
            long remaining = this.getRemaining(player);
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

    private static final long NON_WEAPON_TAG = 5000L;

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
                player.sendMessage(ChatColor.AQUA + "You are now " + getDisplayName() + ChatColor.AQUA + " tagged for " + ChatColor.YELLOW +
                        DurationFormatUtils.formatDurationWords(event.getDuration(), true, true) + ChatColor.AQUA + '.');
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        clearCooldown(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPreventClaimEnterMonitor(PlayerClaimEnterEvent event) {
        if ((event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT) && (!event.getFromFaction().isSafezone() && event.getToFaction().isSafezone())) {
            clearCooldown(event.getPlayer());
        }
    }
}
