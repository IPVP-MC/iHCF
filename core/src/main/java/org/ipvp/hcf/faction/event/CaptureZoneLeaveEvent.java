package org.ipvp.hcf.faction.event;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.ipvp.hcf.eventgame.CaptureZone;
import org.ipvp.hcf.eventgame.faction.CapturableFaction;

/**
 * Faction event called when a player leaves an event capture zone.
 */
public class CaptureZoneLeaveEvent extends FactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final CaptureZone captureZone;
    private final Player player;

    public CaptureZoneLeaveEvent(Player player, CapturableFaction capturableFaction, CaptureZone captureZone) {
        super(capturableFaction);

        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(captureZone, "Capture zone cannot be null");

        this.captureZone = captureZone;
        this.player = player;
    }

    @Override
    public CapturableFaction getFaction() {
        return (CapturableFaction) super.getFaction();
    }

    /**
     * Gets the player leaving this capture zone.
     *
     * @return the player leaving capture zone
     */
    public CaptureZone getCaptureZone() {
        return captureZone;
    }

    /**
     * Gets the capture zone of the faction the player is leaving.
     *
     * @return the leaving capture zone
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}