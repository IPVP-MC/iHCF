package com.doctordark.hcf.timer.event;

import com.doctordark.hcf.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Event called when a {@link Timer} starts.
 */
public class TimerStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Optional<Player> player;
    private final Optional<UUID> userUUID;
    private final Timer timer;
    private final long duration;

    public TimerStartEvent(Timer timer, final long duration) {
        this.player = Optional.empty();
        this.userUUID = Optional.empty();
        this.timer = timer;
        this.duration = duration;
    }

    public TimerStartEvent(@Nullable Player player, UUID uniqueId, Timer timer, long duration) {
        this.player = Optional.ofNullable(player);
        this.userUUID = Optional.ofNullable(uniqueId);
        this.timer = timer;
        this.duration = duration;
    }

    public Optional<Player> getPlayer() {
        return player;
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

    public long getDuration() {
        return duration;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
