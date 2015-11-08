package com.doctordark.hcf.faction.event;

import com.doctordark.hcf.faction.event.cause.FactionLeaveCause;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Faction event called when a user has left their {@link com.doctordark.hcf.faction.type.Faction}.
 */
public class PlayerLeftFactionEvent extends FactionEvent {

    private static final HandlerList handlers = new HandlerList();

    private Optional<Player> player; // lazy-load
    private final UUID uniqueID;
    private final FactionLeaveCause cause;

    public PlayerLeftFactionEvent(Player player, PlayerFaction playerFaction, FactionLeaveCause cause) {
        super(playerFaction);
        this.player = Optional.of(player);
        this.uniqueID = player.getUniqueId();
        this.cause = cause;
    }

    public PlayerLeftFactionEvent(UUID playerUUID, PlayerFaction playerFaction, FactionLeaveCause cause) {
        super(playerFaction);
        this.uniqueID = playerUUID;
        this.cause = cause;
    }

    @Override
    public PlayerFaction getFaction() {
        return (PlayerFaction) super.getFaction();
    }

    /**
     * Gets the optional {@link Player} leaving.
     *
     * @return the {@link Player} or {@link Optional#absent()} or if offline
     */
    public Optional<Player> getPlayer() {
        if (this.player == null) {
            this.player = Optional.fromNullable(Bukkit.getPlayer(uniqueID));
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
     * Gets the cause of leaving the faction.
     *
     * @return the leave cause
     */
    public FactionLeaveCause getCause() {
        return cause;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}

