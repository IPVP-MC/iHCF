package com.doctordark.hcf.faction.event;

import com.doctordark.hcf.faction.event.cause.FactionLeaveCause;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Faction event called when a user is about to leave their {@link Faction}.
 */
public class PlayerLeaveFactionEvent extends FactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Optional<Player> player; // lazy-load
    private final UUID uniqueID;
    private final FactionLeaveCause cause;

    public PlayerLeaveFactionEvent(Player player, PlayerFaction playerFaction, FactionLeaveCause cause) {
        super(playerFaction);

        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(playerFaction, "Player faction cannot be null");
        Preconditions.checkNotNull(cause, "Leave cause cannot be null");

        this.player = Optional.of(player);
        this.uniqueID = player.getUniqueId();
        this.cause = cause;
    }

    public PlayerLeaveFactionEvent(UUID playerUUID, PlayerFaction playerFaction, FactionLeaveCause cause) {
        super(playerFaction);

        Preconditions.checkNotNull(playerUUID, "Player UUID cannot be null");
        Preconditions.checkNotNull(playerFaction, "Player faction cannot be null");
        Preconditions.checkNotNull(cause, "Leave cause cannot be null");

        this.uniqueID = playerUUID;
        this.cause = cause;
    }

    /**
     * Gets the optional {@link Player} leaving.
     *
     * @return the {@link Player} or {@link Optional#absent()} or if offline
     */
    public Optional<Player> getPlayer() {
        if (player == null) {
            player = Optional.fromNullable(Bukkit.getPlayer(uniqueID));
        }

        return player;
    }

    /**
     * Gets the {@link UUID} of the {@link Player} leaving.
     *
     * @return the {@link UUID}
     */
    public UUID getUniqueID() {
        return uniqueID;
    }

    /**
     * Gets the cause of leaving.
     *
     * @return the leave cause
     */
    public FactionLeaveCause getLeaveCause() {
        return cause;
    }

    public static HandlerList getHandlerList() {
        return handlers;
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

