package com.doctordark.hcf.timer.event;

import com.doctordark.hcf.timer.Timer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Optional;
import java.util.UUID;

/**
 * Event called when the pause state of a {@link Timer} changes.
 */
public class TimerPauseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final boolean paused;
    private final Optional<UUID> userUUID;
    private final Timer timer;

    public TimerPauseEvent(Timer timer, boolean paused) {
        this.userUUID = Optional.empty();
        this.timer = timer;
        this.paused = paused;
    }

    public TimerPauseEvent(UUID userUUID, Timer timer, boolean paused) {
        this.userUUID = Optional.ofNullable(userUUID);
        this.timer = timer;
        this.paused = paused;
    }

    /**
     * Gets the optional UUID of the user this has expired for.
     * <p>This may return absent if the timer is not of a player type</p>
     *
     * @return the expiring user UUID or {@link Optional#empty()}
     */
    public Optional<UUID> getUserUUID() {
        return userUUID;
    }

    /**
     * Gets the {@link Timer} that was expired.
     *
     * @return the expiring timer
     */
    public Timer getTimer() {
        return timer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
