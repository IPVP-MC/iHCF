package com.doctordark.hcf.combatlog;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when an {@link LoggerEntity} spawns.
 */
public class LoggerSpawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final LoggerEntity loggerEntity;

    public LoggerSpawnEvent(LoggerEntity loggerEntity) {
        this.loggerEntity = loggerEntity;
    }

    /**
     * Gets the {@link LoggerEntity} spawning.
     *
     * @return the spawning {@link LoggerEntity}
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
