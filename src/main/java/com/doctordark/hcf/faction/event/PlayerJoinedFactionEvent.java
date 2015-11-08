package com.doctordark.hcf.faction.event;

import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Event called when a user has joined a {@link Faction}.
 */
public class PlayerJoinedFactionEvent extends FactionEvent {

    private static final HandlerList handlers = new HandlerList();

    private Optional<Player> player; // lazy-load
    private final UUID uniqueID;

    public PlayerJoinedFactionEvent(Player player, PlayerFaction playerFaction) {
        super(playerFaction);
        this.player = Optional.of(player);
        this.uniqueID = player.getUniqueId();
    }

    public PlayerJoinedFactionEvent(UUID playerUUID, PlayerFaction playerFaction) {
        super(playerFaction);
        this.uniqueID = playerUUID;
    }

    @Override
    public PlayerFaction getFaction() {
        return (PlayerFaction) faction;
    }

    /**
     * Gets the optional {@link Player} joining.
     *
     * @return the {@link Player} or {@link Optional#absent()} or if offline
     */
    public Optional<Player> getPlayer() {
        if (player == null) {
            this.player = Optional.fromNullable(Bukkit.getPlayer(this.uniqueID));
        }

        return player;
    }

    /**
     * Gets the {@link UUID} of the {@link Player} joining.
     *
     * @return the {@link UUID}
     */
    public UUID getUniqueID() {
        return uniqueID;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}