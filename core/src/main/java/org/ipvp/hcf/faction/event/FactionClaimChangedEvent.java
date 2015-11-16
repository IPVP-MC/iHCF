package org.ipvp.hcf.faction.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ipvp.hcf.faction.claim.Claim;
import org.ipvp.hcf.faction.event.cause.ClaimChangeCause;
import org.ipvp.hcf.faction.type.ClaimableFaction;

import java.util.Collection;

/**
 * Faction event called when a {@link Claim} has been claimed by a {@link ClaimableFaction}.
 */
public class FactionClaimChangedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final CommandSender sender;
    private final ClaimChangeCause cause;
    private final Collection<Claim> affectedClaims;

    public FactionClaimChangedEvent(CommandSender sender, ClaimChangeCause cause, Collection<Claim> affectedClaims) {
        this.sender = sender;
        this.cause = cause;
        this.affectedClaims = affectedClaims;
    }

    /**
     * Gets the {@link CommandSender} that made this claim.
     *
     * @return the claiming {@link CommandSender}, null if none
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the {@link ClaimChangeCause} of this event.
     *
     * @return the {@link ClaimChangeCause}
     */
    public ClaimChangeCause getCause() {
        return cause;
    }

    /**
     * Gets the {@link Claim}s being affected.
     *
     * @return the {@link Claim}s being affected
     */
    public Collection<Claim> getAffectedClaims() {
        return affectedClaims;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}