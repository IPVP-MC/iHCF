package com.doctordark.hcf.faction.struct;

import com.doctordark.hcf.faction.type.Faction;

/**
 * Represents a {@link Faction} that can be raided.
 * <p>When a faction has 0 or below DTR (deaths until raidable), they are considered 'raidable'.</p>
 */
public interface Raidable {

    /**
     * Checks if this is raidable.
     *
     * @return true if is raidable
     */
    boolean isRaidable();

    /**
     * Gets the remaining deaths until this becomes 'raidable'.
     *
     * @return the deaths until raidable value
     */
    double getDeathsUntilRaidable();

    /**
     * Gets the maximum deaths until this faction becomes 'raidable'.
     *
     * @return maximum deaths until raidable
     */
    double getMaximumDeathsUntilRaidable();

    /**
     * Sets the remaining deaths until this becomes 'raidable'.
     *
     * @param deathsUntilRaidable the value to set
     * @return the new deaths until raidable value
     */
    double setDeathsUntilRaidable(double deathsUntilRaidable);

    /**
     * Gets the remaining time in milliseconds until this can
     * start regenerating DTR again.
     *
     * @return the time in milliseconds
     */
    long getRemainingRegenerationTime();

    /**
     * Sets the remaining time in milliseconds until this can
     * start regenerating DTR again.
     *
     * @param millis the time to set
     */
    void setRemainingRegenerationTime(long millis);

    /**
     * Gets the {@link RegenStatus} of this.
     *
     * @return the {@link RegenStatus}
     */
    RegenStatus getRegenStatus();
}
