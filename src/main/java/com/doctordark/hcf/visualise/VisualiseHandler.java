package com.doctordark.hcf.visualise;

import com.doctordark.util.cuboid.Cuboid;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R4.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class VisualiseHandler {

    private final Table<UUID, Location, VisualBlock> storedVisualises = HashBasedTable.create();

    public Table<UUID, Location, VisualBlock> getStoredVisualises() {
        return storedVisualises;
    }

    /**
     * Gets a {@link VisualBlock} for a {@link Player}.
     *
     * @param player the {@link Player} to get for
     * @param x      the x co-ordinate to get at
     * @param y      the y co-ordinate to get at
     * @param z      the z co-ordinate to get at
     * @return the {@link VisualBlock} or none
     * @throws NullPointerException if player or location is null
     * @deprecated recommended to use Location based constructor
     */
    @Deprecated
    public VisualBlock getVisualBlockAt(Player player, int x, int y, int z) throws NullPointerException {
        return this.getVisualBlockAt(player, new Location(player.getWorld(), x, y, z));
    }

    /**
     * Gets a {@link VisualBlock} for a {@link Player}.
     *
     * @param player   the {@link Player} to get for
     * @param location the {@link Location} to get at
     * @return the {@link VisualBlock} or none
     * @throws NullPointerException if player or location is null
     */
    public VisualBlock getVisualBlockAt(Player player, Location location) throws NullPointerException {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(location, "Location cannot be null");
        synchronized (storedVisualises) {
            return storedVisualises.get(player.getUniqueId(), location);
        }
    }

    /**
     * Gets the current {@link VisualBlock}s to their {@link Location}s that are shown
     * to a {@link Player} of a specific {@link VisualType}.
     *
     * @param player the {@link Player} to get for
     * @return copied map of {@link VisualBlock}s shown to a {@link Player}.
     */
    public Map<Location, VisualBlock> getVisualBlocks(Player player) {
        synchronized (storedVisualises) {
            return new HashMap<>(storedVisualises.row(player.getUniqueId()));
        }
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
        return Maps.filterValues(this.getVisualBlocks(player), new Predicate<VisualBlock>() {
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

        return this.generate(player, locations, visualType, canOverwrite);
    }

    public LinkedHashMap<Location, VisualBlockData> generateAsync(Player player, Cuboid cuboid, VisualType visualType, boolean canOverwrite) {
        Collection<Location> locations = new HashSet<>(cuboid.getSizeX() * cuboid.getSizeY() * cuboid.getSizeZ());
        Iterator<Location> iterator = cuboid.locationIterator();
        while (iterator.hasNext()) {
            locations.add(iterator.next());
        }

        return this.generateAsync(player, locations, visualType, canOverwrite);
    }

    public LinkedHashMap<Location, VisualBlockData> generate(Player player, Iterable<Location> locations, VisualType visualType, boolean canOverwrite) {
        synchronized (storedVisualises) {
            LinkedHashMap<Location, VisualBlockData> results = new LinkedHashMap<>();

            ArrayList<VisualBlockData> filled = visualType.blockFiller().bulkGenerate(player, locations);
            if (filled != null) {
                int count = 0;
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
                    player.sendBlockChange(location, visualBlockData.getBlockType(), visualBlockData.getData());
                    storedVisualises.put(player.getUniqueId(), location, new VisualBlock(visualType, visualBlockData, location));
                }
            }

            return results;
        }
    }

    public LinkedHashMap<Location, VisualBlockData> generateAsync(Player player, Iterable<Location> locations, VisualType visualType, boolean canOverwrite) {
        synchronized (storedVisualises) {
            LinkedHashMap<Location, VisualBlockData> results = new LinkedHashMap<>();

            ArrayList<VisualBlockData> filled = visualType.blockFiller().bulkGenerate(player, locations);
            if (filled != null) {
                for (Location location : locations) {
                    if (!canOverwrite && storedVisualises.contains(player.getUniqueId(), location)) {
                        continue;
                    }

                    location.getWorld().getChunkAtAsync(location, new World.ChunkLoadCallback() {
                        int count = 0;

                        @Override
                        public void onLoad(Chunk chunk) {
                            Material previousType = CraftMagicNumbers.getMaterial(((CraftChunk) chunk).getHandle().getType(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                            if (previousType.isSolid() || previousType != Material.AIR) {
                                return;
                            }

                            VisualBlockData visualBlockData = filled.get(count++);
                            results.put(location, visualBlockData);
                            player.sendBlockChange(location, visualBlockData.getBlockType(), visualBlockData.getData());
                            storedVisualises.put(player.getUniqueId(), location, new VisualBlock(visualType, visualBlockData, location));
                        }
                    });
                }
            }

            return results;
        }
    }

    /**
     * Clears a visual block at a given location for a player.
     *
     * @param player   the player to clear for
     * @param location the location to clear at
     * @return if the visual block was shown in the first place
     */
    public boolean clearVisualBlock(Player player, Location location) {
        return this.clearVisualBlock(player, location, true);
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
    public boolean clearVisualBlock(Player player, Location location, boolean sendRemovalPacket) {
        synchronized (storedVisualises) {
            VisualBlock visualBlock = this.storedVisualises.remove(player.getUniqueId(), location);
            if (sendRemovalPacket && visualBlock != null) {
                // Have to send a packet to the original block type, don't send if the fake block has the same data properties though.
                Block block = location.getBlock();
                VisualBlockData visualBlockData = visualBlock.getBlockData();
                if (visualBlockData.getBlockType() != block.getType() || visualBlockData.getData() != block.getData()) {
                    player.sendBlockChange(location, block.getType(), block.getData());
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Clears all visual blocks that are shown to a player.
     *
     * @param player the player to clear for
     * @return mapping of the removed visualises
     */
    public Map<Location, VisualBlock> clearVisualBlocks(Player player) {
        return this.clearVisualBlocks(player, null, null);
    }

    /**
     * Clears all visual blocks that are shown to a player of a given VisualType.
     *
     * @param player     the player to clear for
     * @param visualType the visual type
     * @param predicate  the predicate to filter to
     * @return mapping of the removed visualises
     */
    public Map<Location, VisualBlock> clearVisualBlocks(Player player, @Nullable VisualType visualType, @Nullable Predicate<VisualBlock> predicate) {
        return this.clearVisualBlocks(player, visualType, predicate, true);
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
     * @return mapping of the removed visualises
     */
    @Deprecated
    public Map<Location, VisualBlock> clearVisualBlocks(Player player, @Nullable VisualType visualType, @Nullable Predicate<VisualBlock> predicate,
                                                        boolean sendRemovalPackets) {

        synchronized (storedVisualises) {
            if (!this.storedVisualises.containsRow(player.getUniqueId())) return Collections.emptyMap();
            Map<Location, VisualBlock> results = new HashMap<>(this.storedVisualises.row(player.getUniqueId())); // copy to prevent commodification
            Map<Location, VisualBlock> removed = new HashMap<>();
            for (Map.Entry<Location, VisualBlock> entry : results.entrySet()) {
                VisualBlock visualBlock = entry.getValue();


                if ((predicate == null || predicate.apply(visualBlock)) && (visualType == null || visualBlock.getVisualType() == visualType)) {
                    Location location = entry.getKey();
                    if (removed.put(location, visualBlock) == null) { // not really necessary, but might as well
                        this.clearVisualBlock(player, location, sendRemovalPackets); // this will call remove on storedVisualises.
                    }
                }
            }

            return removed;
        }
    }
}
