package org.ipvp.hcf.pvpclass.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.ipvp.hcf.pvpclass.PvpClass;

/**
 * Event called when a player equips a {@link PvpClass}.
 */
public class PvpClassEquipEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final PvpClass pvpClass;

    public PvpClassEquipEvent(Player player, PvpClass pvpClass) {
        super(player);
        this.pvpClass = pvpClass;
    }

    /**
     * Gets the {@link PvpClass} being unequipped.
     *
     * @return the unequipped {@link PvpClass}
     */
    public PvpClass getPvpClass() {
        return pvpClass;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
