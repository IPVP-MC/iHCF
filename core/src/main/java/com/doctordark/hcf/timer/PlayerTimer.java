package com.doctordark.hcf.timer;

import com.doctordark.hcf.timer.event.TimerClearEvent;
import com.doctordark.hcf.timer.event.TimerExtendEvent;
import com.doctordark.hcf.timer.event.TimerPauseEvent;
import com.doctordark.hcf.timer.event.TimerStartEvent;
import com.doctordark.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Represents a {@link Player} {@link Timer} countdown.
 */
public abstract class PlayerTimer extends Timer {

    protected final boolean persistable;
    protected final Map<UUID, TimerCooldown> cooldowns = new ConcurrentHashMap<>();

    public PlayerTimer(String name, long defaultCooldown) {
        this(name, defaultCooldown, true);
    }

    public PlayerTimer(String name, long defaultCooldown, boolean persistable) {
        super(name, defaultCooldown);
        this.persistable = persistable;
    }

    /**
     * Handles what happens when this {@link PlayerTimer} expires for a user.
     * <p>Extending this requires calling the super first</p>
     *
     * @param player     the {@link Player} if online, otherwise null
     * @param playerUUID the UUID of user to handle for
     */
    protected void handleExpiry(@Nullable Player player, UUID playerUUID) {
        cooldowns.remove(playerUUID);
    }

    public TimerCooldown clearCooldown(UUID uuid) {
        return clearCooldown(null, uuid);
    }

    public TimerCooldown clearCooldown(Player player) {
        Objects.requireNonNull(player);
        return clearCooldown(player, player.getUniqueId());
    }

    public TimerCooldown clearCooldown(@Nullable Player player, UUID playerUUID) {
        TimerCooldown runnable = cooldowns.remove(playerUUID);
        if (runnable != null) {
            runnable.cancel();
            if (player == null) {
                Bukkit.getPluginManager().callEvent(new TimerClearEvent(playerUUID, this));
            } else {
                Bukkit.getPluginManager().callEvent(new TimerClearEvent(player, this));
            }
        }

        return runnable;
    }

    public boolean isPaused(Player player) {
        return isPaused(player.getUniqueId());
    }

    public boolean isPaused(UUID playerUUID) {
        TimerCooldown runnable = cooldowns.get(playerUUID);
        return runnable != null && runnable.isPaused();
    }

    public void setPaused(UUID playerUUID, boolean paused) {
        TimerCooldown runnable = cooldowns.get(playerUUID);
        if (runnable != null && runnable.isPaused() != paused) {
            TimerPauseEvent event = new TimerPauseEvent(playerUUID, this, paused);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                runnable.setPaused(paused);
            }
        }
    }

    public long getRemaining(Player player) {
        return getRemaining(player.getUniqueId());
    }

    public long getRemaining(UUID playerUUID) {
        TimerCooldown runnable = cooldowns.get(playerUUID);
        return runnable == null ? 0L : runnable.getRemaining();
    }

    public boolean setCooldown(@Nullable Player player, UUID playerUUID) {
        return setCooldown(player, playerUUID, defaultCooldown, false);
    }

    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite) {
        return setCooldown(player, playerUUID, duration, overwrite, null);
    }

    /**
     * @return true if cooldown was set or changed
     */
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite, @Nullable Predicate<Long> currentCooldownPredicate) {
        TimerCooldown runnable = duration > 0L ? cooldowns.get(playerUUID) : clearCooldown(player, playerUUID);
        if (runnable != null) {
            long remaining = runnable.getRemaining();
            if (!overwrite && remaining > 0L && duration <= remaining) {
                return false;
            }

            TimerExtendEvent event = new TimerExtendEvent(player, playerUUID, this, remaining, duration);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            boolean flag = true;
            if (currentCooldownPredicate != null) {
                flag = currentCooldownPredicate.test(remaining);
            }

            if (flag) {
                runnable.setRemaining(duration);
            }

            return flag;
        } else {
            Bukkit.getPluginManager().callEvent(new TimerStartEvent(player, playerUUID, this, duration));
            runnable = new TimerCooldown(this, playerUUID, duration);
        }

        cooldowns.put(playerUUID, runnable);
        return true;
    }

    private static final String COOLDOWN_PATH = "timer-cooldowns";
    private static final String PAUSE_PATH = "timer-pauses";

    @Override
    public void load(Config config) {
        if (!persistable) {
            return;
        }

        String path = COOLDOWN_PATH + '.' + name;
        Object object = config.get(path);
        if (object instanceof MemorySection) {
            MemorySection section = (MemorySection) object;
            long millis = System.currentTimeMillis();
            for (String id : section.getKeys(false)) {
                long remaining = config.getLong(section.getCurrentPath() + '.' + id) - millis;
                if (remaining > 0L) {
                    setCooldown(null, UUID.fromString(id), remaining, true, null);
                }
            }
        }

        path = PAUSE_PATH + '.' + name;
        if ((object = config.get(path)) instanceof MemorySection) {
            MemorySection section = (MemorySection) object;
            for (String id : section.getKeys(false)) {
                TimerCooldown timerCooldown = cooldowns.get(UUID.fromString(id));
                if (timerCooldown == null) continue;

                timerCooldown.setPauseMillis(config.getLong(path + '.' + id));
            }
        }
    }

    @Override
    public void onDisable(Config config) {
        if (persistable) {
            Set<Map.Entry<UUID, TimerCooldown>> entrySet = cooldowns.entrySet();
            Map<String, Long> pauseSavemap = new LinkedHashMap<>(entrySet.size());
            Map<String, Long> cooldownSavemap = new LinkedHashMap<>(entrySet.size());
            for (Map.Entry<UUID, TimerCooldown> entry : entrySet) {
                String id = entry.getKey().toString();
                TimerCooldown runnable = entry.getValue();
                pauseSavemap.put(id, runnable.getPauseMillis());
                cooldownSavemap.put(id, runnable.getExpiryMillis());
            }

            config.set("timer-pauses." + name, pauseSavemap);
            config.set("timer-cooldowns." + name, cooldownSavemap);
        }
    }
}