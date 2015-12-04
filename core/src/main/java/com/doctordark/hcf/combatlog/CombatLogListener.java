package com.doctordark.hcf.combatlog;

import com.doctordark.hcf.ConfigurationService;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.combatlog.event.LoggerRemovedEvent;
import com.doctordark.hcf.combatlog.event.LoggerSpawnEvent;
import com.doctordark.hcf.combatlog.type.LoggerEntity;
import com.doctordark.hcf.combatlog.type.LoggerEntityHuman;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

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

    private final Set<UUID> safelyDisconnected = new HashSet<>();
    private final Map<UUID, LoggerEntity> loggers = new HashMap<>();

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
    public void safelyDisconnect(Player player, String reason) {
        if (this.safelyDisconnected.add(player.getUniqueId())) {
            player.kickPlayer(reason);
        }
    }

    /**
     * Removes all the {@link LoggerEntity} instances from the server.
     */
    public void removeCombatLoggers() {
        Iterator<LoggerEntity> iterator = this.loggers.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().destroy();
            iterator.remove();
        }

        this.safelyDisconnected.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        EntityPlayer entityPlayer = ((CraftPlayer) event.getEntity()).getHandle();
        if (entityPlayer instanceof LoggerEntityHuman) {
            entityPlayer.getBukkitEntity().saveData();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoggerDeath(LoggerRemovedEvent event) {
        this.loggers.remove(event.getLoggerEntity().getPlayerUUID());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        LoggerEntity currentLogger = this.loggers.remove(event.getPlayer().getUniqueId());
        if (currentLogger != null) {
            CraftLivingEntity loggerEntity = currentLogger.getBukkitEntity();
            currentLogger.destroy();
            Player player = event.getPlayer();
            new BukkitRunnable() {
                @Override
                public void run() {
                    WorldServer worldServer = (WorldServer) loggerEntity.getHandle().world;
                    worldServer.getTracker().untrackEntity(((CraftPlayer) player).getHandle());
                    worldServer.getTracker().track(((CraftPlayer) player).getHandle());
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean result = this.safelyDisconnected.remove(uuid);
        if (!ConfigurationService.COMBAT_LOG_PREVENTION_ENABLED) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (player.getGameMode() != GameMode.CREATIVE && !player.isDead() && !result) {
            // If the player has PVP protection, don't spawn a logger
            if (plugin.getTimerManager().getInvincibilityTimer().getRemaining(uuid) > 0L) {
                return;
            }

            // There is no enemies near the player, so don't spawn a logger.
            if (this.plugin.getTimerManager().getTeleportTimer().getNearbyEnemies(player, NEARBY_SPAWN_RADIUS) <= 0) {
                return;
            }

            // Make sure the player is not in a safezone.
            Location location = player.getLocation();
            if (this.plugin.getFactionManager().getFactionAt(location).isSafezone()) {
                return;
            }

            // Make sure the player hasn't already spawned a logger.
            if (this.loggers.containsKey(player.getUniqueId())) {
                return;
            }

            // Run a tick later so the actual
            // player NBT can be saved properly.
            new BukkitRunnable() {
                @Override
                public void run() {
                    LoggerEntity loggerEntity = new LoggerEntityHuman(plugin, player, location.getWorld());
                    LoggerSpawnEvent calledEvent = new LoggerSpawnEvent(loggerEntity);
                    Bukkit.getPluginManager().callEvent(calledEvent);
                    if (calledEvent.isCancelled()) {
                        return;
                    }

                    loggers.put(player.getUniqueId(), loggerEntity);

                    // Make the NPC spawn with the players stuff for cosmetic effect
                    CraftHumanEntity craftEntity = loggerEntity.getBukkitEntity();
                    PlayerInventory loggerInventory = craftEntity.getInventory();
                    loggerInventory.setContents(inventory.getContents());
                    loggerInventory.setArmorContents(inventory.getArmorContents());
                    craftEntity.addPotionEffects(player.getActivePotionEffects());
                }
            }.runTask(plugin);
        }
    }
}
