package com.doctordark.hcf.timer;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.event.TimerExpireEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class TimerCooldown {

    private BukkitTask eventNotificationTask;

    @Getter
    private final Timer timer;

    private final UUID owner;        // the UUID of user that this timer runnable is for

    @Getter
    private long expiryMillis;       // the milliseconds until this expires

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private long pauseMillis;        // the milliseconds that were remaining when was paused

    protected TimerCooldown(Timer timer, long duration) {
        this.owner = null;
        this.timer = timer;
        this.setRemaining(duration);
    }

    protected TimerCooldown(Timer timer, UUID playerUUID, long duration) {
        this.timer = timer;
        this.owner = playerUUID;
        this.setRemaining(duration);
    }

    public long getRemaining() {
        return getRemaining(false);
    }

    protected long getRemaining(boolean ignorePaused) {
        if (!ignorePaused && pauseMillis != 0L) {
            return pauseMillis; // If isn't paused, return that.
        } else {
            return expiryMillis - System.currentTimeMillis();
        }
    }

    protected void setRemaining(long milliseconds) throws IllegalStateException {
        if (milliseconds <= 0L) {
            cancel();
            return;
        }

        long expiryMillis = System.currentTimeMillis() + milliseconds;
        if (expiryMillis != this.expiryMillis) {
            this.expiryMillis = expiryMillis;

            // Recreate the task manually as Bukkit doesn't allow
            // you to just reschedule for some reason :(.
            if (eventNotificationTask != null) {
                eventNotificationTask.cancel();
            }

            long ticks = milliseconds / 50L;
            eventNotificationTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (timer instanceof PlayerTimer && owner != null) {
                        ((PlayerTimer) timer).handleExpiry(Bukkit.getPlayer(owner), owner);
                    }

                    Bukkit.getPluginManager().callEvent(new TimerExpireEvent(owner, timer));
                }
            }.runTaskLater(HCF.getPlugin(), ticks);
        }
    }

    protected boolean isPaused() {
        return pauseMillis != 0L;
    }

    public void setPaused(boolean paused) {
        if (paused != isPaused()) {
            if (paused) {
                pauseMillis = getRemaining(true);
                cancel();
            } else {
                setRemaining(pauseMillis);
                pauseMillis = 0L;
            }
        }
    }

    /**
     * Cancels this runnable for event notification.
     *
     * @throws IllegalStateException if was not running
     */
    protected void cancel() throws IllegalStateException {
        if (eventNotificationTask != null) {
            eventNotificationTask.cancel();
            eventNotificationTask = null;
        }
    }
}
