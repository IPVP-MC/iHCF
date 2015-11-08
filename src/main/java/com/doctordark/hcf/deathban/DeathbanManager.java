package com.doctordark.hcf.deathban;

import gnu.trove.map.TObjectIntMap;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface DeathbanManager {

    long MAX_DEATHBAN_TIME = TimeUnit.HOURS.toMillis(8);

    /**
     * Gets the map storing the lives with the {@link UUID} string by the amount.
     *
     * @return the lives map
     */
    TObjectIntMap<UUID> getLivesMap();

    /**
     * Gets the lives of a player.
     *
     * @param uuid the uuid of player to get for
     * @return the amount of owned lives
     */
    int getLives(UUID uuid);

    /**
     * Sets the amount of lives a player has.
     *
     * @param uuid   the uuid of player to set for
     * @param amount the amount to set at
     * @return the new lives of the player
     */
    int setLives(UUID uuid, int amount);

    /**
     * Gives lives to a player.
     *
     * @param uuid   the uuid of player to give to
     * @param amount the amount to give
     * @return the new lives of the player
     */
    int addLives(UUID uuid, int amount);

    /**
     * Takes lives away from a player.
     *
     * @param uuid   the uuid of player to take for
     * @param amount the amount to take
     * @return the new lives of the player
     */
    int takeLives(UUID uuid, int amount);

    /**
     * Gets the deathban multiplier for a {@link Player}.
     *
     * @param player the {@link Player} to get for
     * @return the deathban multiplier
     */
    double getDeathBanMultiplier(Player player);

    /**
     * Applies a {@link Deathban} to a {@link Player}.
     *
     * @param player the {@link Player} to apply to
     * @param reason the reason for {@link Deathban}
     * @return the {@link Deathban} that has been applied
     */
    Deathban applyDeathBan(Player player, String reason);

    /**
     * Applies a {@link Deathban} to a {@link Player}.
     *
     * @param uuid     the uuid of player to apply to
     * @param deathban the {@link Deathban} to be applied
     * @return the {@link Deathban} that has been applied
     */
    Deathban applyDeathBan(UUID uuid, Deathban deathban);

    /**
     * Reloads deathban data from storage.
     */
    void reloadDeathbanData();

    /**
     * Saves deathban data to storage.
     */
    void saveDeathbanData();
}
