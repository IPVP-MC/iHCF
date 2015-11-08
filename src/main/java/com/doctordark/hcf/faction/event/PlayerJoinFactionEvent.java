package com.doctordark.hcf.faction.event;

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
 * Event called when a user is about to join a {@link Faction}.
 */
public class PlayerJoinFactionEvent extends FactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Optional<Player> player; // lazy-load
    private final UUID uniqueID;

    public PlayerJoinFactionEvent(Player player, PlayerFaction playerFaction) {
        super(playerFaction);
        Preconditions.checkNotNull(player, "Player cannot be null");
        this.player = Optional.of(player);
        this.uniqueID = player.getUniqueId();
    }

    public PlayerJoinFactionEvent(UUID playerUUID, PlayerFaction playerFaction) {
        super(playerFaction);
        Preconditions.checkNotNull(playerUUID, "Player UUID cannot be null");
        this.uniqueID = playerUUID;
    }

    /**
     * Gets the optional {@link Player} joining.
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
     * Gets the {@link UUID} of the {@link Player} joining.
     *
     * @return the {@link UUID}
     */
    public UUID getUniqueID() {
        return uniqueID;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
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