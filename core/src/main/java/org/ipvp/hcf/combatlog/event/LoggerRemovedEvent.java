package org.ipvp.hcf.combatlog.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.ipvp.hcf.combatlog.type.LoggerEntity;

public class LoggerRemovedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final LoggerEntity loggerEntity;

    public LoggerRemovedEvent(LoggerEntity loggerEntity) {
        this.loggerEntity = loggerEntity;
    }

    public LoggerEntity getLoggerEntity() {
        return this.loggerEntity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
