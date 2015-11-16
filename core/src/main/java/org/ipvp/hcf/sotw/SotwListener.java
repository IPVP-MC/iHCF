package org.ipvp.hcf.sotw;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.ipvp.hcf.HCF;

public class SotwListener implements Listener {

    private final HCF plugin;

    public SotwListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() != EntityDamageEvent.DamageCause.SUICIDE && plugin.getSotwTimer().getSotwRunnable() != null) {
            event.setCancelled(true);
        }
    }
}
