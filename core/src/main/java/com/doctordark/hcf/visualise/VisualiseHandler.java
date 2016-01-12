package com.doctordark.hcf.visualise;

import com.doctordark.util.cuboid.Cuboid;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.spigotmc.AsyncCatcher;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class VisualiseHandler {

    private final Table<UUID, Location, VisualBlock> storedVisualises = HashBasedTable.create();

    /**
     * Gets a {@link VisualBlock} for a {@link Player}.
     *
     * @param player   the {@link Player} to get for
     * @param location the {@link Location} to get at
     * @return the {@link VisualBlock} or none
     * @throws NullPointerException if player or location is null
     */
    public VisualBlock getVisualBlockAt(Player player, Location location) throws NullPointerException {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(location, "Location cannot be null");
        return storedVisualises.get(player.getUniqueId(), location);
    }

    /**
     * Gets the current {@link VisualBlock}s to their {@link Location}s that are shown
     * to a {@link Player} of a specific {@link VisualType}.
     *
     * @param player the {@link Player} to get for
     * @return copied map of {@link VisualBlock}s shown to a {@link Player}.
     */
    public Map<Location, VisualBlock> getVisualBlocks(Player player) {
        return new HashMap<>(storedVisualises.row(player.getUniqueId()));
    }

    /**
     * Gets the current {@link VisualBlock}s to their {@link Location}s that are shown
     * to a {@link Player} of a specific {@link VisualType}.
     *
     * @param player     the {@link Player} to get for
     * @param visualType the {@link VisualType} to get for
     * @return copied map of {@link VisualBlock}s shown to a {@link Player}.
     */
    public Map<Location, VisualBlock> getVisualBlocks(Player player, VisualType visualType) {
        return Maps.filterValues(getVisualBlocks(player), new com.google.common.base.Predicate<VisualBlock>() {
            @Override
            public boolean apply(VisualBlock visualBlock) {
                return visualType == visualBlock.getVisualType();
            }
        });
    }

    public LinkedHashMap<Location, VisualBlockData> generate(Player player, Cuboid cuboid, VisualType visualType, boolean canOverwrite) {
        Collection<Location> locations = new HashSet<>(cuboid.getSizeX() * cuboid.getSizeY() * cuboid.getSizeZ());
        Iterator<Location> iterator = cuboid.locationIterator();
        while (iterator.hasNext()) {
            locations.add(iterator.next());
        }

        return generate(player, locations, visualType, canOverwrite);
    }

    public LinkedHashMap<Location, VisualBlockData> generate(Player player, Iterable<Location> locations, VisualType visualType, boolean canOverwrite) {
        LinkedHashMap<Location, VisualBlockData> results = new LinkedHashMap<>();

        ArrayList<VisualBlockData> filled = visualType.blockFiller().bulkGenerate(player, locations);
        if (filled != null) {
            int count = 0;
            Map<Location, MaterialData> updatedBlocks = new HashMap<>();
            for (Location location : locations) {
                if (!canOverwrite && storedVisualises.contains(player.getUniqueId(), location)) {
                    continue;
                }

                Material previousType = location.getBlock().getType();
                if (previousType.isSolid() || previousType != Material.AIR) {
                    continue;
                }

                VisualBlockData visualBlockData = filled.get(count++);
                results.put(location, visualBlockData);
                updatedBlocks.put(location, visualBlockData);
                storedVisualises.put(player.getUniqueId(), location, new VisualBlock(visualType, visualBlockData, location));
            }

            try {
                VisualiseUtil.handleBlockChanges(player, updatedBlocks);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return results;
    }

    /**
     * Clears a visual block at a given location for a player.
     *
     * @param player   the player to clear for
     * @param location the location to clear at
     * @return if the visual block was shown in the first place
     */
    public void clearVisualBlock(Player player, Location location) {
        clearVisualBlock(player, location, true);
    }

    /**
     * Clears a visual block at a given location for a player.
     *
     * @param player            the player to clear for
     * @param location          the location to clear at
     * @param sendRemovalPacket if a packet to send a block change should be sent
     *                          (this is used to prevent unnecessary packets sent when
     *                          disconnecting or changing worlds, for example)
     * @return if the visual block was shown in the first place
     */
    public void clearVisualBlock(Player player, Location location, boolean sendRemovalPacket) {
        VisualBlock visualBlock = storedVisualises.remove(player.getUniqueId(), location);
        if (sendRemovalPacket && visualBlock != null) {
            // Have to send a packet to the original block type, don't send if the fake block has the same data properties though.
            Block block = location.getBlock();
            VisualBlockData visualBlockData = visualBlock.getBlockData();
            if (visualBlockData.getBlockType() != block.getType() || visualBlockData.getData() != block.getData()) {
                player.sendBlockChange(location, block.getType(), block.getData());
            }
        }
    }

    /**
     * Clears all visual blocks in a {@link Chunk}.
     *
     * @param chunk the {@link Chunk} to clear in
     */
    public void clearVisualBlocks(Chunk chunk) {
        AsyncCatcher.catchOp("Chunk operation");
        if (!storedVisualises.isEmpty()) {
            Set<Location> keys = storedVisualises.columnKeySet();
            for (Location location : new HashSet<>(keys)) {
                if (location.getWorld().equals(chunk.getWorld()) && chunk.getX() == (((int) location.getX()) >> 4) && chunk.getZ() == (((int) location.getZ()) >> 4)) {
                    keys.remove(location);
                }
            }
        }
    }

    /**
     * Clears all visual blocks that are shown to a player.
     *
     * @param player the player to clear for
     */
    public void clearVisualBlocks(Player player) {
        clearVisualBlocks(player, null, null);
    }

    /**
     * Clears all visual blocks that are shown to a player of a given VisualType.
     *
     * @param player     the player to clear for
     * @param visualType the visual type
     * @param predicate  the predicate to filter to
     */
    public void clearVisualBlocks(Player player, @Nullable VisualType visualType, @Nullable Predicate<VisualBlock> predicate) {
        clearVisualBlocks(player, visualType, predicate, true);
    }

    /**
     * Clears all visual blocks that are shown to a player of a given VisualType.
     *
     * @param player             the player to clear for
     * @param visualType         the visual type
     * @param predicate          the predicate to filter to
     * @param sendRemovalPackets if a packet to send a block change should be sent
     *                           (this is used to prevent unnecessary packets sent when
     *                           disconnecting or changing worlds, for example)
     */
    @Deprecated
    public void clearVisualBlocks(Player player,
                                  @Nullable VisualType visualType,
                                  @Nullable Predicate<VisualBlock> predicate,
                                  boolean sendRemovalPackets) {

        if (!storedVisualises.containsRow(player.getUniqueId())) {
            return;
        }

        Map<Location, VisualBlock> results = new HashMap<>(storedVisualises.row(player.getUniqueId())); // copy to prevent commodification
        Map<Location, VisualBlock> removed = new HashMap<>();
        for (Map.Entry<Location, VisualBlock> entry : results.entrySet()) {
            VisualBlock visualBlock = entry.getValue();
            if ((predicate == null || predicate.test(visualBlock)) && (visualType == null || visualBlock.getVisualType() == visualType)) {
                Location location = entry.getKey();
                if (removed.put(location, visualBlock) == null) { // not really necessary, but might as well
                    clearVisualBlock(player, location, sendRemovalPackets); // this will call remove on storedVisualises.
                }
            }
        }
    }
}
