package org.ipvp.hcf.faction.type;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.ipvp.hcf.ConfigurationService;
import org.ipvp.hcf.faction.claim.Claim;

import java.util.Map;

/**
 * Represents the {@link SpawnFaction}.
 */
public class SpawnFaction extends ClaimableFaction implements ConfigurationSerializable {

    public SpawnFaction() {
        super("Spawn");

        this.safezone = true;
        for (World world : Bukkit.getWorlds()) {
            int radius = ConfigurationService.SPAWN_RADIUS_MAP.get(world.getEnvironment());
            if (radius > 0) {
                addClaim(new Claim(this, new Location(world, radius, 0, radius), new Location(world, -radius, world.getMaxHeight(), -radius)), null);
            }
        }
    }

    public SpawnFaction(Map<String, Object> map) {
        super(map);
    }

    @Override
    public boolean isDeathban() {
        return false;
    }
}
