package org.ipvp.hcf.combatlog.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ipvp.hcf.combatlog.type.LoggerEntity;

public class LoggerSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final LoggerEntity loggerEntity;

    public LoggerSpawnEvent(LoggerEntity loggerEntity) {
        this.loggerEntity = loggerEntity;
    }

    public LoggerEntity getLoggerEntity() {
        return this.loggerEntity;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
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
