package org.ipvp.hcf.faction.event;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.ipvp.hcf.faction.type.Faction;
import org.ipvp.hcf.faction.type.PlayerFaction;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Event called when a user has joined a {@link Faction}.
 */
public class PlayerJoinedFactionEvent extends FactionEvent {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Optional<Player> player; // lazy-load

    @Getter
    private final CommandSender sender;

    @Getter
    private final UUID playerUUID;

    public PlayerJoinedFactionEvent(CommandSender sender, @Nullable Player player, UUID playerUUID, PlayerFaction playerFaction) {
        super(playerFaction);

        Preconditions.checkNotNull(sender, "Sender cannot be null");
        Preconditions.checkNotNull(playerUUID, "Player UUID cannot be null");
        Preconditions.checkNotNull(playerFaction, "Player faction cannot be null");

        this.sender = sender;
        if (player != null) {
            this.player = Optional.of(player);
        }

        this.playerUUID = playerUUID;
    }

    /**
     * Gets the optional {@link Player} joining, this will load lazily.
     *
     * @return the {@link Player} or {@link Optional#absent()} or if offline
     */
    public Optional<Player> getPlayer() {
        if (this.player == null) {
            this.player = Optional.fromNullable(Bukkit.getPlayer(this.playerUUID));
        }

        return this.player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public PlayerFaction getFaction() {
        return (PlayerFaction) super.getFaction();
    }
}