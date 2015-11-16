package org.ipvp.hcf.listener.fixes;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

/**
 * Listener that prevents boats from being placed on land.
 */
public class BoatGlitchFixListener implements Listener {

    // Makes it so boats can only be placed on water.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof Boat) {
            Boat boat = (Boat) vehicle;
            Block belowBlock = boat.getLocation().add(0, -1, 0).getBlock();
            if (belowBlock.getType() != Material.WATER && belowBlock.getType() != Material.STATIONARY_WATER) {
                boat.remove();
            }
        }
    }
}
