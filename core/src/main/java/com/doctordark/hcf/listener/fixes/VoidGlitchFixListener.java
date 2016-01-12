package com.doctordark.hcf.listener.fixes;

import com.doctordark.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Listener that prevents players being killed by the void in the Overworld.
 */
public class VoidGlitchFixListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Entity entity = event.getEntity();
            if (entity instanceof Player) {
                // Allow players to die by VOID in the END
                if (entity.getWorld().getEnvironment() == World.Environment.THE_END) {
                    return;
                }

                Location destination = BukkitUtils.getHighestLocation(entity.getLocation());
                if (destination != null && entity.teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                    event.setCancelled(true);
                    ((Player) entity).sendMessage(ChatColor.YELLOW + "You were saved from the void.");
                }
            }
        }
    }
}
