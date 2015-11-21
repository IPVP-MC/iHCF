package com.doctordark.hcf.sotw;

import com.doctordark.hcf.HCF;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

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
