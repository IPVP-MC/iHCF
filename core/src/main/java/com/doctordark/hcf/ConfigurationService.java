package com.doctordark.hcf;

import org.bukkit.World;

import java.util.EnumMap;
import java.util.Map;

//TODO: This needs to be removed.
@Deprecated
public final class ConfigurationService {

    public static final int END_PORTAL_RADIUS = 20;
    public static final int END_PORTAL_CENTER = 500;
    public static final Map<World.Environment, Integer> ROAD_LENGTHS = new EnumMap<>(World.Environment.class);
    public static final Map<World.Environment, Integer> SPAWN_RADIUS_MAP = new EnumMap<>(World.Environment.class);

    static {
        ROAD_LENGTHS.put(World.Environment.NORMAL, 4000);
        ROAD_LENGTHS.put(World.Environment.NETHER, 4000);

        SPAWN_RADIUS_MAP.put(World.Environment.NORMAL, 50);
        SPAWN_RADIUS_MAP.put(World.Environment.NETHER, 25);
        SPAWN_RADIUS_MAP.put(World.Environment.THE_END, 15);
    }
}
