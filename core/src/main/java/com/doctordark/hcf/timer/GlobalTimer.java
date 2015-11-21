package com.doctordark.hcf.timer;

import com.doctordark.hcf.timer.event.TimerExtendEvent;
import com.doctordark.hcf.timer.event.TimerPauseEvent;
import com.doctordark.hcf.timer.event.TimerStartEvent;
import org.bukkit.Bukkit;

/**
 * Represents a global {@link Timer}.
 */
public abstract class GlobalTimer extends Timer {

    private TimerCooldown runnable;

    public GlobalTimer(String name, long defaultCooldown) {
        super(name, defaultCooldown);
    }

    public boolean clearCooldown() {
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
            return true;
        }

        return false;
    }

    public boolean isPaused() {
        return runnable != null && runnable.isPaused();
    }

    public void setPaused(boolean paused) {
        if (runnable != null && runnable.isPaused() != paused) {
            TimerPauseEvent event = new TimerPauseEvent(this, paused);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                runnable.setPaused(paused);
            }
        }
    }

    public long getRemaining() {
        return runnable == null ? 0L : runnable.getRemaining();
    }

    public boolean setRemaining() {
        return setRemaining(defaultCooldown, false);
    }

    public boolean setRemaining(long duration, boolean overwrite) {
        boolean hadCooldown = false;
        if (runnable != null) {
            if (!overwrite) return false;

            TimerExtendEvent event = new TimerExtendEvent(this, runnable.getRemaining(), duration);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;

            hadCooldown = runnable.getRemaining() > 0L;
            runnable.setRemaining(duration);
        } else {
            Bukkit.getPluginManager().callEvent(new TimerStartEvent(this, duration));
            runnable = new TimerCooldown(this, duration);
        }

        return !hadCooldown;
    }
}
