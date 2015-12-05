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
        return this.getRemaining(false);
    }

    protected long getRemaining(boolean ignorePaused) {
        if (!ignorePaused && this.pauseMillis != 0L) {
            return this.pauseMillis; // If isn't paused, return that.
        }

        return this.expiryMillis - System.currentTimeMillis();
    }

    protected void setRemaining(long milliseconds) throws IllegalStateException {
        if (milliseconds <= 0L) {
            this.cancel();
            return;
        }

        long expiryMillis = System.currentTimeMillis() + milliseconds;
        if (expiryMillis != this.expiryMillis) {
            this.expiryMillis = expiryMillis;

            // Recreate the task manually as Bukkit doesn't allow
            // you to just reschedule for some reason :(.
            if (this.eventNotificationTask != null) {
                this.eventNotificationTask.cancel();
            }

            long ticks = milliseconds / 50L;
            this.eventNotificationTask = new BukkitRunnable() {
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
        return this.pauseMillis != 0L;
    }

    public void setPaused(boolean paused) {
        if (paused != this.isPaused()) {
            if (paused) {
                this.pauseMillis = this.getRemaining(true);
                this.cancel();
            } else {
                this.setRemaining(this.pauseMillis);
                this.pauseMillis = 0L;
            }
        }
    }

    /**
     * Cancels this runnable for event notification.
     *
     * @throws IllegalStateException if was not running
     */
    protected void cancel() throws IllegalStateException {
        if (this.eventNotificationTask != null) {
            this.eventNotificationTask.cancel();
            this.eventNotificationTask = null;
        }
    }
}
