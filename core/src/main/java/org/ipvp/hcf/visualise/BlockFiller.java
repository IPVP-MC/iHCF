package org.ipvp.hcf.visualise;

import com.google.common.collect.Iterables;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Represents how visual blocks are filled.
 */
abstract class BlockFiller {

    /**
     * @deprecated recommended to use version with location constructor
     */
    @Deprecated
    VisualBlockData generate(Player player, World world, int x, int y, int z) {
        return generate(player, new Location(world, x, y, z));
    }

    abstract VisualBlockData generate(Player player, Location location);

    ArrayList<VisualBlockData> bulkGenerate(Player player, Iterable<Location> locations) {
        ArrayList<VisualBlockData> data = new ArrayList<>(Iterables.size(locations));
        for (Location location : locations) {
            data.add(this.generate(player, location));
        }

        return data;
    }
}
