package org.ipvp.hcf.faction.event;

import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ipvp.hcf.faction.claim.Claim;
import org.ipvp.hcf.faction.event.cause.ClaimChangeCause;
import org.ipvp.hcf.faction.type.ClaimableFaction;

import java.util.Collection;

/**
 * Event called when {@link Claim}s are about to be changed.
 */
public class FactionClaimChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final ClaimChangeCause cause;
    private final Collection<Claim> affectedClaims;
    private final ClaimableFaction claimableFaction;
    private final CommandSender sender;

    public FactionClaimChangeEvent(CommandSender sender, ClaimChangeCause cause, Collection<Claim> affectedClaims, ClaimableFaction claimableFaction) {
        Preconditions.checkNotNull(sender, "CommandSender cannot be null");
        Preconditions.checkNotNull(cause, "Cause cannot be null");
        Preconditions.checkNotNull(affectedClaims, "Affected claims cannot be null");
        Preconditions.checkNotNull(affectedClaims.isEmpty(), "Affected claims cannot be empty");
        Preconditions.checkNotNull(claimableFaction, "ClaimableFaction cannot be null");

        this.sender = sender;
        this.cause = cause;
        this.affectedClaims = affectedClaims;
        this.claimableFaction = claimableFaction;
    }

    /**
     * Gets the {@link CommandSender} that made this claim.
     *
     * @return the claiming sender
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the cause of this event.
     *
     * @return the event cause
     */
    public ClaimChangeCause getCause() {
        return cause;
    }

    /**
     * Gets the {@link Claim}s being changed during this event.
     *
     * @return collection of affected {@link Claim}s
     */
    public Collection<Claim> getAffectedClaims() {
        return affectedClaims;
    }

    /**
     * GEts the {@link ClaimableFaction} making these changes.
     *
     * @return the {@link ClaimableFaction}
     */
    public ClaimableFaction getClaimableFaction() {
        return claimableFaction;
    }

    public static HandlerList getHandlerList() {
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}