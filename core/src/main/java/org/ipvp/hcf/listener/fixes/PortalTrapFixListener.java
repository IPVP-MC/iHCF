package org.ipvp.hcf.listener.fixes;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Listener that prevents {@link Player}s from being trapped in portals.
 */
public class PortalTrapFixListener implements Listener {

    /*private final Map<UUID, BukkitTask> portalTasks = new HashMap<>();
    private final HCF plugin;

    public PortalTrapFixListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onPlayerPortal2(PlayerPortalEvent event) {
        //event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            Player player = event.getPlayer();
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            BukkitTask task = portalTasks.remove(player.getUniqueId());
            if (task != null) task.cancel();

            portalTasks.put(player.getUniqueId(), new BukkitRunnable() {
                @Override
                public void run() {
                    entityPlayer.portalCooldown = 0;
                    entityPlayer.ah();
                }
            }.runTaskLater(plugin, 80L));
        }
    }*/
}