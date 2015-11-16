package org.ipvp.hcf.faction.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ipvp.hcf.faction.struct.Raidable;
import org.ipvp.hcf.faction.type.Faction;

/**
 * Event called when the DTR of a {@link Faction} is changed.
 */
public class FactionDtrChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final DtrUpdateCause cause;
    private final Raidable raidable;
    private final double originalDtr;
    private double newDtr;

    public FactionDtrChangeEvent(DtrUpdateCause cause, Raidable raidable, double originalDtr, double newDtr) {
        this.cause = cause;
        this.raidable = raidable;
        this.originalDtr = originalDtr;
        this.newDtr = newDtr;
    }

    public DtrUpdateCause getCause() {
        return cause;
    }

    public Raidable getRaidable() {
        return raidable;
    }

    public double getOriginalDtr() {
        return originalDtr;
    }

    public double getNewDtr() {
        return newDtr;
    }

    public void setNewDtr(double newDtr) {
        this.newDtr = newDtr;
    }

    public enum DtrUpdateCause {
        REGENERATION, MEMBER_DEATH
    }

    @Override
    public boolean isCancelled() {
        return cancelled || (Math.abs(newDtr - originalDtr) == 0);
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
