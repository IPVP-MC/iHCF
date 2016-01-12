package com.doctordark.hcf.timer.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.pvpclass.PvpClass;
import com.doctordark.hcf.timer.PlayerTimer;
import com.doctordark.hcf.timer.TimerCooldown;
import com.doctordark.util.Config;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.EquipmentSetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Timer that handles {@link PvpClass} warmups.
 */
public class PvpClassWarmupTimer extends PlayerTimer implements Listener {

    protected final Map<UUID, PvpClass> classWarmups = new HashMap<>();

    private final HCF plugin;

    public PvpClassWarmupTimer(HCF plugin) {
        super("Class Warmup", TimeUnit.SECONDS.toMillis(10L), false);
        this.plugin = plugin;

        // Re-equip the applicable class for every player during reloads.
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    attemptEquip(player);
                }
            }
        }.runTaskLater(plugin, 10L);
    }

    @Override
    public void onDisable(Config config) {
        super.onDisable(config);
        classWarmups.clear();
    }

    @Override
    public String getScoreboardPrefix() {
        return ChatColor.AQUA + ChatColor.BOLD.toString();
    }

    @Override
    public TimerCooldown clearCooldown(@Nullable Player player, UUID playerUUID) {
        TimerCooldown runnable = super.clearCooldown(player, playerUUID);
        if (runnable != null) {
            classWarmups.remove(playerUUID);
        }

        return runnable;
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID userUUID) {
        PvpClass pvpClass = classWarmups.remove(userUUID);
        if (player != null) {
            Preconditions.checkNotNull(pvpClass, "Attempted to equip a class for %s, but nothing was added", player.getName());
            plugin.getPvpClassManager().setEquippedClass(player, pvpClass);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerQuitEvent event) {
        plugin.getPvpClassManager().setEquippedClass(event.getPlayer(), null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        attemptEquip(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEquipmentSet(EquipmentSetEvent event) {
        HumanEntity humanEntity = event.getHumanEntity();
        if (humanEntity instanceof Player) {
            attemptEquip((Player) humanEntity);
        }
    }

    private void attemptEquip(Player player) {
        PvpClass current = plugin.getPvpClassManager().getEquippedClass(player);
        if (current != null) {
            if (current.isApplicableFor(player)) {
                return;
            }

            plugin.getPvpClassManager().setEquippedClass(player, null);
        } else if ((current = classWarmups.get(player.getUniqueId())) != null) {
            if (current.isApplicableFor(player)) {
                return;
            }

            clearCooldown(player);
        }

        Collection<PvpClass> pvpClasses = plugin.getPvpClassManager().getPvpClasses();
        for (PvpClass pvpClass : pvpClasses) {
            if (pvpClass.isApplicableFor(player)) {
                classWarmups.put(player.getUniqueId(), pvpClass);
                setCooldown(player, player.getUniqueId(), pvpClass.getWarmupDelay(), false);
                break;
            }
        }
    }
}
