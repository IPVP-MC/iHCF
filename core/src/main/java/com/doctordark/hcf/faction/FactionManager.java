package com.doctordark.hcf.faction;

import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Interface for managing {@link Faction}s in the plugin.
 */
public interface FactionManager {

    long MAX_DTR_REGEN_MILLIS = TimeUnit.HOURS.toMillis(3L);
    String MAX_DTR_REGEN_WORDS = DurationFormatUtils.formatDurationWords(MAX_DTR_REGEN_MILLIS, true, true);

    Map<String, ?> getFactionNameMap();

    /**
     * Gets all the available {@link Faction}s held by this manager.
     *
     * @return an immutable list of {@link Faction}s
     */
    ImmutableList<Faction> getFactions();

    /**
     * Gets the optional {@link Claim} at a given {@link Location}.
     *
     * @param location the {@link Location} to look at
     * @return the {@link Claim} at the given {@link Location}
     */
    Claim getClaimAt(Location location);

    /**
     * Gets the optional {@link Claim} at given co-ordinates.
     *
     * @param world the world to get claim in
     * @param x     the x co-ordinate to get at
     * @param z     the z co-ordinate to get at
     * @return the claim at co-ordinates
     */
    Claim getClaimAt(World world, int x, int z);

    /**
     * Gets the faction which owns a {@link Claim} at a
     * given {@link Location}.
     *
     * @param location the {@link Location} to check
     * @return the {@link Faction} owning the claim at {@link Location}
     */
    Faction getFactionAt(Location location);

    /**
     * Gets the faction which owns a {@link Claim} at the position of a given {@link Block}.
     *
     * @param block the {@link Block} to get at
     * @return the {@link Faction} owning the claim at {@link Block}
     */
    Faction getFactionAt(Block block);

    /**
     * Gets a {@link Faction} which owns a {@link Claim} at specific
     * co-ordinates.
     *
     * @param world the {@link World} to get for
     * @param x     the x coordinate to get at
     * @param z     the z coordinate to get at
     * @return the {@link Faction} owning the {@link Claim}
     */
    Faction getFactionAt(World world, int x, int z);

    /**
     * Gets a {@link Faction} with a given name.
     *
     * @param factionName the search string to lookup
     * @return {@link Faction} with name of id
     */
    Faction getFaction(String factionName);

    /**
     * Gets a {@link Faction} with a given UUID.
     *
     * @param factionUUID the search UUID to lookup
     * @return {@link Faction} with uuid
     */
    Faction getFaction(UUID factionUUID);

    /**
     * Gets a {@link PlayerFaction} containing a member who has
     * a {@link UUID} String equal to the given search string.
     *
     * @param search the search string to lookup
     * @return the {@link PlayerFaction} with member of id
     * @deprecated use of {@link org.bukkit.Bukkit#getOfflinePlayer(UUID)} in main thread
     */
    @Deprecated
    PlayerFaction getContainingPlayerFaction(String search);

    /**
     * Gets a {@link PlayerFaction} of a {@link org.bukkit.entity.Player}.
     *
     * @param player the {@link org.bukkit.entity.Player} to lookup
     * @return the {@link PlayerFaction} containing {@link org.bukkit.entity.Player}
     * @deprecated use of {@link org.bukkit.Bukkit#getOfflinePlayer(UUID)} in main thread
     */
    @Deprecated
    PlayerFaction getPlayerFaction(Player player);

    /**
     * Gets a {@link PlayerFaction} by a UUID of a player.
     *
     * @param uuid the uuid of player to lookup
     * @return the {@link PlayerFaction} containing player with uuid
     */
    PlayerFaction getPlayerFaction(UUID uuid);

    /**
     * Gets a {@link Faction} with a given name or containing a
     * {@link FactionMember} with the given name.
     *
     * @param id the id to search for
     * @return the {@link Faction}
     */
    Faction getContainingFaction(String id);

    /**
     * Checks if the manager holds a {@link Faction}.
     *
     * @param faction the {@link Faction} to check
     * @return true if manager contains the {@link Faction}
     */
    boolean containsFaction(Faction faction);

    /**
     * Creates a {@link Faction} for this manager.
     *
     * @param faction the {@link Faction} to add
     * @return true if the {@link Faction} successfully created
     */
    boolean createFaction(Faction faction);

    /**
     * Saves a {@link Faction} to this manager by a
     * given {@link CommandSender}.
     *
     * @param sender  the {@link CommandSender} of creations
     * @param faction the {@link Faction} to add
     * @return true if the {@link Faction} was successfully created
     */
    boolean createFaction(Faction faction, CommandSender sender);

    /**
     * Removes a {@link Faction} from this manager by a
     * given {@link CommandSender}.
     *
     * @param faction the {@link Faction} to remove
     * @param sender  the {@link CommandSender} of removal
     * @return true if the {@link Faction} was successfully removed
     */
    boolean removeFaction(Faction faction, CommandSender sender);

    /**
     * Loads the {@link Faction} data from storage.
     */
    void reloadFactionData();

    /**
     * Saves the {@link Faction} data to storage.
     */
    void saveFactionData();
}