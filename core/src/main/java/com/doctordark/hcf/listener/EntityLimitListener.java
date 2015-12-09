package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Listener that limits the amount of entities in one chunk.
 */
public class EntityLimitListener implements Listener {

    private static final int MAX_CHUNK_GENERATED_ENTITIES = 25;
    private static final int MAX_NATURAL_CHUNK_ENTITIES = 25;

    private final HCF plugin;

    public EntityLimitListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!plugin.getConfiguration().isHandleEntityLimiting()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Squid) {
            event.setCancelled(true);
            return;
        }

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) { // allow slimes to always split
            switch (event.getSpawnReason()) {
                case NATURAL:
                    if (event.getLocation().getChunk().getEntities().length > MAX_NATURAL_CHUNK_ENTITIES) {
                        event.setCancelled(true);
                    }
                    break;
                case CHUNK_GEN:
                    if (event.getLocation().getChunk().getEntities().length > MAX_CHUNK_GENERATED_ENTITIES) {
                        event.setCancelled(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
