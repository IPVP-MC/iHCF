package com.doctordark.hcf.faction.event;

import com.doctordark.hcf.faction.type.Faction;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event called when a {@link Faction} is about to be renamed.
 */
public class FactionRenameEvent extends FactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final CommandSender sender;
    private final String originalName;
    private String newName;

    public FactionRenameEvent(Faction faction, CommandSender sender, String originalName, String newName) {
        super(faction);
        this.sender = sender;
        this.originalName = originalName;
        this.newName = newName;
    }

    /**
     * Gets the {@link CommandSender} attempting to remove this {@link Faction}.
     *
     * @return the {@link CommandSender}
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the name before the {@link Faction} decided to
     * perform the rename.
     *
     * @return the original name
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * Gets the name the {@link Faction} is attempting to change to
     *
     * @return the new name
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Sets the new name to provide for this faction.
     *
     * @param newName the new name to set
     */
    public void setNewName(String newName) {
        if (!newName.equals(this.newName)) {
            this.newName = newName;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled || (originalName.equals(newName));
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
