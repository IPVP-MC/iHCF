package com.doctordark.hcf.combatlog;

import com.doctordark.hcf.ConfigurationService;
import com.doctordark.hcf.HCF;
import com.doctordark.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Listener that prevents {@link Player}s from combat-logging.
 */
public class CombatLogListener implements Listener {

    private static final int NEARBY_SPAWN_RADIUS = 64;

    private static final Set<UUID> SAFE_DISCONNECTS = new HashSet<>();
    private static final Map<UUID, CombatLogEntry> LOGGERS = new HashMap<>();

    private final HCF plugin;

    public CombatLogListener(HCF plugin) {
        this.plugin = plugin;
    }

    /**
     * Disconnects a {@link Player} without a {@link LoggerEntity} spawning.
     *
     * @param player the {@link Player} to disconnect
     * @param reason the reason for disconnecting
     */
    public static void safelyDisconnect(Player player, String reason) {
        if (SAFE_DISCONNECTS.add(player.getUniqueId())) {
            player.kickPlayer(reason);
        }
    }

    /**
     * Removes all the {@link LoggerEntity} instances from the server.
     */
    public static void removeCombatLoggers() {
        Iterator<CombatLogEntry> iterator = LOGGERS.values().iterator();
        while (iterator.hasNext()) {
            CombatLogEntry entry = iterator.next();
            entry.task.cancel();
            entry.loggerEntity.getBukkitEntity().remove();
            iterator.remove();
        }

        SAFE_DISCONNECTS.clear();
    }

    // Add this, just in case the NPC doesn't despawn safely.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLoggerInteract(EntityInteractEvent event) {
        Collection<CombatLogEntry> entries = LOGGERS.values();
        for (CombatLogEntry entry : entries) {
            if (entry.loggerEntity.getBukkitEntity().equals(event.getEntity())) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoggerDeath(LoggerDeathEvent event) {
        CombatLogEntry entry = LOGGERS.remove(event.getLoggerEntity().getPlayerUUID());
        if (entry != null) {
            entry.task.cancel();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        CombatLogEntry combatLogEntry = LOGGERS.remove(event.getPlayer().getUniqueId());
        if (combatLogEntry != null) {
            CraftLivingEntity loggerEntity = combatLogEntry.loggerEntity.getBukkitEntity();

            // Apply some attributes back to the player.
            Player player = event.getPlayer();
            event.setSpawnLocation(loggerEntity.getLocation());
            player.setFallDistance(loggerEntity.getFallDistance());
            player.setHealth(Math.min(player.getMaxHealth(), loggerEntity.getHealth()));
            if (loggerEntity.getTicksLived() > 20) {
                player.setTicksLived(loggerEntity.getTicksLived());
            }

            player.setRemainingAir(loggerEntity.getRemainingAir());

            // Finally remove.
            loggerEntity.remove();
            combatLogEntry.task.cancel();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean result = SAFE_DISCONNECTS.remove(uuid);
        if (!ConfigurationService.COMBAT_LOG_PREVENTION_ENABLED) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (player.getGameMode() != GameMode.CREATIVE && !player.isDead() && !result) {
            // If the player has an empty inventory or PVP protection, don't spawn a logger
            if (InventoryUtils.isEmpty(inventory) || plugin.getTimerManager().getInvincibilityTimer().getRemaining(uuid) > 0L) {
                return;
            }

            // There is no enemies near the player, so don't spawn a logger.
            if (plugin.getTimerManager().getTeleportTimer().getNearbyEnemies(player, NEARBY_SPAWN_RADIUS) <= 0) {
                return;
            }

            // Make sure the player is not in a safezone.
            Location location = player.getLocation();
            if (plugin.getFactionManager().getFactionAt(location).isSafezone()) {
                return;
            }

            // Make sure the player hasn't already spawned a logger.
            if (LOGGERS.containsKey(player.getUniqueId())) {
                return;
            }

            World world = location.getWorld();
            LoggerEntity loggerEntity = new LoggerEntity(world, location, player);

            LoggerSpawnEvent calledEvent = new LoggerSpawnEvent(loggerEntity);
            Bukkit.getPluginManager().callEvent(calledEvent);

            // Make the NPC spawn with the players stuff for cosmetic effect, and make a task to remove this entity from the server.
            LOGGERS.put(uuid, new CombatLogEntry(loggerEntity, new LoggerRemovable(uuid, loggerEntity).runTaskLater(plugin, ConfigurationService.COMBAT_LOG_DESPAWN_TICKS)));
            CraftEntity craftEntity = loggerEntity.getBukkitEntity();
            if (craftEntity != null) {
                CraftLivingEntity craftLivingEntity = (CraftLivingEntity) craftEntity;
                EntityEquipment entityEquipment = craftLivingEntity.getEquipment();
                entityEquipment.setItemInHand(inventory.getItemInHand());
                entityEquipment.setArmorContents(inventory.getArmorContents());
                craftLivingEntity.addPotionEffects(player.getActivePotionEffects());
            }
        }
    }

    private static class LoggerRemovable extends BukkitRunnable {

        private final UUID uuid;
        private final LoggerEntity loggerEntity;

        public LoggerRemovable(UUID uuid, LoggerEntity loggerEntity) {
            this.uuid = uuid;
            this.loggerEntity = loggerEntity;
        }

        @Override
        public void run() {
            if (LOGGERS.remove(uuid) != null) {
                loggerEntity.dead = true;
            }
        }
    }
}
