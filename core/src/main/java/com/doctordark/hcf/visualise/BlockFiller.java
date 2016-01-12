package com.doctordark.hcf.visualise;

import com.google.common.collect.Iterables;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Represents how visual blocks are filled.
 */
abstract class BlockFiller {

    abstract VisualBlockData generate(Player player, Location location);

    ArrayList<VisualBlockData> bulkGenerate(Player player, Iterable<Location> locations) {
        ArrayList<VisualBlockData> data = new ArrayList<>(Iterables.size(locations));
        for (Location location : locations) {
            data.add(generate(player, location));
        }

        return data;
    }
}
