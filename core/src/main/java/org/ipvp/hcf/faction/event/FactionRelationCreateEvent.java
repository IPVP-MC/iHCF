package org.ipvp.hcf.faction.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ipvp.hcf.faction.struct.Relation;
import org.ipvp.hcf.faction.type.PlayerFaction;

/**
 * Faction event called a {@link PlayerFaction} has created a {@link Relation} with another {@link PlayerFaction}.
 */
public class FactionRelationCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final PlayerFaction senderFaction;
    private final PlayerFaction targetFaction;
    private final Relation relation;

    public FactionRelationCreateEvent(PlayerFaction senderFaction, PlayerFaction targetFaction, Relation relation) {
        this.senderFaction = senderFaction;
        this.targetFaction = targetFaction;
        this.relation = relation;
    }

    /**
     * Gets the {@link PlayerFaction} sending the request.
     *
     * @return the requesting {@link PlayerFaction}
     */
    public PlayerFaction getSenderFaction() {
        return senderFaction;
    }

    /**
     * Gets the {@link PlayerFaction} asked to accept request.
     *
     * @return the targeted {@link PlayerFaction}
     */
    public PlayerFaction getTargetFaction() {
        return targetFaction;
    }

    public Relation getRelation() {
        return relation;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}