package com.doctordark.hcf.faction.claim;

import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.GenericUtils;
import com.doctordark.util.cuboid.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A implementation of {@link Claim} that denies other {@link FactionMember}s to access specific areas.
 */
public class Subclaim extends Claim implements Cloneable, ConfigurationSerializable {

    private final Set<UUID> accessibleMembers = new HashSet<>();

    public Subclaim(Map<String, Object> map) {
        super(map);
        accessibleMembers.addAll(GenericUtils.createList(map.get("accessibleMembers"), String.class).stream().map(UUID::fromString).collect(Collectors.toList()));
    }

    public Subclaim(Faction faction, Location location) {
        super(faction, location, location);
    }

    public Subclaim(Faction faction, Location location1, Location location2) {
        super(faction, location1, location2);
    }

    public Subclaim(Faction faction, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(faction, world, x1, y1, z1, x2, y2, z2);
    }

    public Subclaim(Faction faction, Cuboid cuboid) {
        super(faction, cuboid);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.remove("subclaims");
        map.put("accessibleMembers", accessibleMembers.stream().map(UUID::toString).collect(Collectors.toList()));
        return map;
    }

    /**
     * Gets the member {@link UUID}s that have access to this {@link Subclaim}.
     *
     * @return set of accessible member {@link UUID}s
     */
    public Set<UUID> getAccessibleMembers() {
        return accessibleMembers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subclaim)) return false;
        if (!super.equals(o)) return false;

        Subclaim blocks = (Subclaim) o;

        return !(accessibleMembers != null ? !accessibleMembers.equals(blocks.accessibleMembers) : blocks.accessibleMembers != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (accessibleMembers != null ? accessibleMembers.hashCode() : 0);
        return result;
    }

    @Override
    public Subclaim clone() {
        return (Subclaim) super.clone();
    }
}
