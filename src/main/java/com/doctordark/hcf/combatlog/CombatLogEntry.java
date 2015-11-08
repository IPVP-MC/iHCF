package com.doctordark.hcf.combatlog;

import org.bukkit.scheduler.BukkitTask;

/**
 * Represents an entry for a {@link org.bukkit.entity.Player} that has combat logged allowing to lookup events that happened at the time
 */
public class CombatLogEntry {

    public final LoggerEntity loggerEntity;
    public final BukkitTask task;

    public CombatLogEntry(LoggerEntity loggerEntity, BukkitTask task) {
        this.loggerEntity = loggerEntity;
        this.task = task;
    }
}


