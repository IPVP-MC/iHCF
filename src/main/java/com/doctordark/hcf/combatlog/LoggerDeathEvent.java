package com.doctordark.hcf.combatlog;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when an {@link LoggerEntity} dies.
 */
public class LoggerDeathEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final LoggerEntity loggerEntity;

    public LoggerDeathEvent(LoggerEntity loggerEntity) {
        this.loggerEntity = loggerEntity;
    }

    /**
     * Gets the {@link LoggerEntity} being killed.
     *
     * @return the killed {@link LoggerEntity}
     */
    public LoggerEntity getLoggerEntity() {
        return loggerEntity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
