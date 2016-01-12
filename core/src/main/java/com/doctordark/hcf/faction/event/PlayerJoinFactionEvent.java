package com.doctordark.hcf.faction.event;

import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Event called when a user is about to join a {@link Faction}.
 */
public class PlayerJoinFactionEvent extends FactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Optional<Player> player; // lazy-load

    @Getter
    private final CommandSender sender;

    @Getter
    private final UUID playerUUID;

    public PlayerJoinFactionEvent(CommandSender sender, @Nullable Player player, UUID playerUUID, PlayerFaction playerFaction) {
        super(playerFaction);

        Objects.requireNonNull(sender, "Sender cannot be null");
        Objects.requireNonNull(playerUUID, "Player UUID cannot be null");
        Objects.requireNonNull(playerFaction, "Player faction cannot be null");

        this.sender = sender;
        if (player != null) {
            this.player = Optional.of(player);
        }

        this.playerUUID = playerUUID;
    }

    public Optional<Player> getPlayer() {
        if (this.player == null) {
            this.player = Optional.ofNullable(Bukkit.getPlayer(this.playerUUID));
        }

        return this.player;
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

    @Override
    public PlayerFaction getFaction() {
        return (PlayerFaction) super.getFaction();
    }
}