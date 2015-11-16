package org.ipvp.hcf.faction.type;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.ipvp.hcf.ConfigurationService;
import org.ipvp.hcf.faction.claim.Claim;

import java.util.Map;

/**
 * Represents the {@link EndPortalFaction}.
 */
public class EndPortalFaction extends ClaimableFaction implements ConfigurationSerializable {

    public EndPortalFaction() {
        super("EndPortal");

        World overworld = Bukkit.getWorld("world");
        int maxHeight = overworld.getMaxHeight();
        int min = ConfigurationService.END_PORTAL_CENTER - ConfigurationService.END_PORTAL_RADIUS;
        int max = ConfigurationService.END_PORTAL_CENTER + ConfigurationService.END_PORTAL_RADIUS;

        // North East (++)
        addClaim(new Claim(this, new Location(overworld, min, 0, min), new Location(overworld, max, maxHeight, max)), null);

        // South West (--)
        addClaim(new Claim(this, new Location(overworld, -max, maxHeight, -max), new Location(overworld, -min, 0, -min)), null);

        // North West (-+)
        addClaim(new Claim(this, new Location(overworld, -max, 0, min), new Location(overworld, -min, maxHeight, max)), null);

        // South East (+-)
        addClaim(new Claim(this, new Location(overworld, min, 0, -max), new Location(overworld, max, maxHeight, -min)), null);

        this.safezone = true;
    }

    public EndPortalFaction(Map<String, Object> map) {
        super(map);
    }

    @Override
    public boolean isDeathban() {
        return false;
    }
}
