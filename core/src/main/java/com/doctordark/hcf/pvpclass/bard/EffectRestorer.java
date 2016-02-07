package com.doctordark.hcf.pvpclass.bard;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.pvpclass.event.PvpClassUnequipEvent;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.ref.WeakReference;
import java.util.Collection;

public class EffectRestorer implements Listener {

    private final Table<Player, PotionEffectType, RestoreTask> restores = HashBasedTable.create();

    public EffectRestorer(HCF plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelRestores(PvpClassUnequipEvent event) {
        cancelRestores(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void cancelRestores(PlayerQuitEvent event) {
        cancelRestores(event.getPlayer());
    }

    private void cancelRestores(Player player) {
        restores.row(player).values().forEach(RestoreTask::cancel);
    }

    private void cancelRestore(Player player, PotionEffectType effect) {
        RestoreTask task = restores.remove(player, effect);
        if (task != null) {
            task.cancel();
        }
    }

    public void setRestoreEffect(Player player, PotionEffect effect) {
        boolean shouldCancel = true;
        Collection<PotionEffect> activeList = player.getActivePotionEffects();
        for (PotionEffect active : activeList) {
            if (!active.getType().equals(effect.getType())) continue;

            // If the current potion effect has a higher amplifier, ignore this one.
            if (effect.getAmplifier() < active.getAmplifier()) {
                return;
            } else if (effect.getAmplifier() == active.getAmplifier()) {
                // If the current potion effect has a longer duration, ignore this one.
                if (effect.getDuration() < active.getDuration()) {
                    return;
                }
            }

            restores.put(player, active.getType(), new RestoreTask(player, active));
            shouldCancel = false;
            break;
        }

        // Cancel the previous restore.
        player.addPotionEffect(effect, true);
        if (shouldCancel && effect.getDuration() > BardClass.HELD_EFFECT_DURATION_TICKS &&
                effect.getDuration() < BardClass.DEFAULT_MAX_DURATION) {
            cancelRestore(player, effect.getType());
        }
    }

    private class RestoreTask implements Runnable {

        private final int taskId;
        private final WeakReference<Player> player;
        private final PotionEffect effect;

        public RestoreTask(Player player, PotionEffect effect) {
            this.player = new WeakReference<>(player);
            this.effect = effect;
            taskId = Bukkit.getScheduler().runTaskLater(HCF.getPlugin(), this, effect.getDuration()).getTaskId();
        }

        @Override
        public void run() {
            // Do nothing if player is not valid.
            Player player = this.player.get();
            if (player == null || !player.isOnline()) return;

            // Restore players' potion effect.
            player.addPotionEffect(effect, true);
        }

        public void cancel() {
            Bukkit.getScheduler().cancelTask(taskId);
        }

    }

}
