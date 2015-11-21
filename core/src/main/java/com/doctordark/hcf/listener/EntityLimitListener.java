package com.doctordark.hcf.listener;

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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) { // allow slimes to always split
            return;
        }

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
