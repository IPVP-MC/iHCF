package com.doctordark.hcf.eventgame.tracker;

import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import org.bukkit.entity.Player;

/**
 * Tracker for handling event mini-games.
 * NOTE: The methods here are called before they happen, so
 * the onControlLoss method for example would still
 * have its' {@link CaptureZone} player unchanged.
 */
@Deprecated
public interface EventTracker {

    EventType getEventType();

    /**
     * Handles ticking every 5 seconds
     *
     * @param eventTimer   the timer
     * @param eventFaction the faction
     */
    void tick(EventTimer eventTimer, EventFaction eventFaction);

    void onContest(EventFaction eventFaction, EventTimer eventTimer);

    boolean onControlTake(Player player, CaptureZone captureZone, EventFaction eventFaction);

    void onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction);

    void stopTiming();
}
