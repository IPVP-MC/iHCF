package com.doctordark.hcf.pvpclass.archer;

import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;

/**
 * Represents a tag made by {@link org.bukkit.entity.Player} who has the {@link ArcherClass} equipped.
 */
public class ArcherMark implements Comparable<ArcherMark> {

    public BukkitTask decrementTask;
    public int currentLevel;

    public void reset() {
        if (this.decrementTask != null) {
            this.decrementTask.cancel();
            this.decrementTask = null;
        }

        this.currentLevel = 0;
    }

    public int incrementMark() {
        return ++this.currentLevel;
    }

    public int decrementMark() {
        return --this.currentLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArcherMark)) return false;

        ArcherMark that = (ArcherMark) o;

        if (currentLevel != that.currentLevel) return false;
        return !(decrementTask != null ? !decrementTask.equals(that.decrementTask) : that.decrementTask != null);
    }

    @Override
    public int hashCode() {
        int result = decrementTask != null ? decrementTask.hashCode() : 0;
        result = 31 * result + currentLevel;
        return result;
    }

    @Override
    public int compareTo(@Nonnull ArcherMark archerMark) {
        return Integer.compare(currentLevel, archerMark.currentLevel);
    }
}