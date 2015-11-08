package com.doctordark.hcf.economy;

import gnu.trove.map.TObjectIntMap;

import java.util.UUID;

/**
 * Handles balances of players.
 */
public interface EconomyManager {

    char ECONOMY_SYMBOL = '$';

    /**
     * Gets the map of economy balances.
     *
     * @return the map of economy balances
     */
    TObjectIntMap<UUID> getBalanceMap();

    /**
     * Gets the balance of a player.
     *
     * @param uuid the uuid of player to get for
     * @return the balance of the player
     */
    int getBalance(UUID uuid);

    /**
     * Sets the balance of a player.
     *
     * @param uuid   the uuid of player to set for
     * @param amount the amount to set
     * @return the new balance of player
     */
    int setBalance(UUID uuid, int amount);

    /**
     * Adds to the balance of a player.
     *
     * @param uuid   the uuid of player to add for
     * @param amount the amount to add
     * @return the new balance of player
     */
    int addBalance(UUID uuid, int amount);

    /**
     * Takes from the balance of a player.
     *
     * @param uuid   the uuid of player to take from
     * @param amount the amount to take
     * @return the new balance of player
     */
    int subtractBalance(UUID uuid, int amount);

    /**
     * Reloads the data from storage.
     */
    void reloadEconomyData();

    /**
     * Saves the data to storage.
     */
    void saveEconomyData();
}
