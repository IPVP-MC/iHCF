package com.doctordark.hcf.faction.claim;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.ClaimableFaction;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.GenericUtils;
import com.doctordark.util.cuboid.Cuboid;
import com.doctordark.util.cuboid.NamedCuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

/**
 * An implementation of a {@link NamedCuboid} that represents land for a {@link Faction} can own.
 */
public class Claim extends NamedCuboid implements Cloneable, ConfigurationSerializable {

    private static final Random RANDOM = new Random();

    private final Map<String, Subclaim> subclaims = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final UUID claimUniqueID;
    private final UUID factionUUID;

    public Claim(Map<String, Object> map) {
        super(map);

        this.name = (String) map.get("name");
        this.claimUniqueID = UUID.fromString((String) map.get("claimUUID"));
        this.factionUUID = UUID.fromString((String) map.get("factionUUID"));
        for (Subclaim subclaim : GenericUtils.createList(map.get("subclaims"), Subclaim.class)) {
            this.subclaims.put(subclaim.getName(), subclaim);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("name", name);
        map.put("claimUUID", claimUniqueID.toString());
        map.put("factionUUID", factionUUID.toString());
        map.put("subclaims", new ArrayList<>(subclaims.values()));
        return map;
    }

    public Claim(Faction faction, Location location) {
        super(location, location);
        this.name = generateName();
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(Faction faction, Location location1, Location location2) {
        super(location1, location2);
        this.name = generateName();
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(Faction faction, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(world, x1, y1, z1, x2, y2, z2);
        this.name = generateName();
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(Faction faction, Cuboid cuboid) {
        super(cuboid);
        this.name = generateName();
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    private String generateName() {
        return String.valueOf(RANDOM.nextInt(899) + 100);
    }

    public UUID getClaimUniqueID() {
        return claimUniqueID;
    }

    private ClaimableFaction faction;
    private boolean loaded = false;

    public ClaimableFaction getFaction() {
        if (!this.loaded) {
            Faction faction = HCF.getPlugin().getFactionManager().getFaction(this.factionUUID);
            if (faction instanceof ClaimableFaction) {
                this.faction = (ClaimableFaction) faction;
            }

            this.loaded = true;
        }

        return this.faction;
    }

    /**
     * Gets the {@link Subclaim}s registered to this {@link Claim}.
     *
     * @return set of registered {@link Subclaim}s
     */
    public Collection<Subclaim> getSubclaims() {
        return this.subclaims.values();
    }

    /**
     * Gets a {@link Subclaim} this {@link Claim} has registered with
     * a specific name.
     *
     * @param name the name to search for
     * @return the {@link Subclaim}, null if not found
     */
    public Subclaim getSubclaim(String name) {
        return subclaims.get(name);
    }

    /**
     * Gets the formatted name for this {@link Claim}.
     *
     * @return the {@link Claim} formatted name
     */
    public String getFormattedName() {
        return getName() + ": (" + worldName + ", " + x1 + ", " + y1 + ", " + z1 + ") - (" + worldName + ", " + x2 + ", " + y2 + ", " + z2 + ')';
    }

    @Override
    public Claim clone() {
        return (Claim) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Claim blocks = (Claim) o;

        if (loaded != blocks.loaded) return false;
        if (subclaims != null ? !subclaims.equals(blocks.subclaims) : blocks.subclaims != null) return false;
        if (claimUniqueID != null ? !claimUniqueID.equals(blocks.claimUniqueID) : blocks.claimUniqueID != null) return false;
        if (factionUUID != null ? !factionUUID.equals(blocks.factionUUID) : blocks.factionUUID != null) return false;
        return !(faction != null ? !faction.equals(blocks.faction) : blocks.faction != null);
    }

    @Override
    public int hashCode() {
        int result = subclaims != null ? subclaims.hashCode() : 0;
        result = 31 * result + (claimUniqueID != null ? claimUniqueID.hashCode() : 0);
        result = 31 * result + (factionUUID != null ? factionUUID.hashCode() : 0);
        result = 31 * result + (faction != null ? faction.hashCode() : 0);
        result = 31 * result + (loaded ? 1 : 0);
        return result;
    }
}
