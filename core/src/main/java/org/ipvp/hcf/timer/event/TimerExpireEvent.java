package org.ipvp.hcf.timer.event;

import org.ipvp.hcf.timer.Timer;
import com.google.common.base.Optional;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event called when a {@link Timer} expires.
 */
public class TimerExpireEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Optional<UUID> userUUID;
    private final Timer timer;

    public TimerExpireEvent(Timer timer) {
        this.userUUID = Optional.absent();
        this.timer = timer;
    }

    public TimerExpireEvent(UUID userUUID, Timer timer) {
        this.userUUID = Optional.fromNullable(userUUID);
        this.timer = timer;
    }

    /**
     * Gets the optional UUID of the user this has expired for.
     * <p>This may return absent if the timer is not of a player type</p>
     *
     * @return the expiring user UUID or {@link Optional#absent()}
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
