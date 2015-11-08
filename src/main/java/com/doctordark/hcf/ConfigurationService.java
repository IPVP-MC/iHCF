package com.doctordark.hcf;

import com.doctordark.util.Config;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

//TODO: Actually configurable
@Deprecated
public final class ConfigurationService {

    public static void init(JavaPlugin plugin) {
        Config config = new Config(plugin, "settings");
        MAX_ALLIES_PER_FACTION = config.getInt("max-allies", MAX_ALLIES_PER_FACTION);
        MAX_MEMBERS_PER_FACTION = config.getInt("max-members", MAX_MEMBERS_PER_FACTION);
    }

    public static final boolean DISABLE_OBSIDIAN_GENERATORS = true;

    public static final TimeZone SERVER_TIME_ZONE = TimeZone.getTimeZone("GMT+1");
    public static final int WARZONE_RADIUS = 850;
    public static final String DONATE_URL = "donate.ipvp.org";

    public static final int SPAWN_BUFFER = 128;
    public static final int MAP_NUMBER = 7;

    public static final boolean KIT_MAP = false;

    public static final List<String> DISALLOWED_FACTION_NAMES = ImmutableList.of("kohieotw", "kohisotw", "hcteams", "hcteamseotw", "hcteamssotw", "para", "parahcf", "parasotw", "paraeotw");

    public static final Map<Enchantment, Integer> ENCHANTMENT_LIMITS = new HashMap<>();
    public static final Map<PotionType, Integer> POTION_LIMITS = new EnumMap<>(PotionType.class);
    public static final Map<World.Environment, Integer> ROAD_LENGTHS = new EnumMap<>(World.Environment.class);
    public static final Map<World.Environment, Integer> SPAWN_RADIUS_MAP = new EnumMap<>(World.Environment.class);
    public static boolean DIAMOND_ORE_ALERTS = true;

    static {
        POTION_LIMITS.put(PotionType.STRENGTH, 0);
        POTION_LIMITS.put(PotionType.WEAKNESS, 0);
        POTION_LIMITS.put(PotionType.SLOWNESS, 0);
        POTION_LIMITS.put(PotionType.INVISIBILITY, 0);
        POTION_LIMITS.put(PotionType.POISON, 0);

        ENCHANTMENT_LIMITS.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ENCHANTMENT_LIMITS.put(Enchantment.DAMAGE_ALL, 1);
        ENCHANTMENT_LIMITS.put(Enchantment.ARROW_KNOCKBACK, 0);
        ENCHANTMENT_LIMITS.put(Enchantment.KNOCKBACK, 0);
        ENCHANTMENT_LIMITS.put(Enchantment.FIRE_ASPECT, 0);
        ENCHANTMENT_LIMITS.put(Enchantment.THORNS, 0);
        ENCHANTMENT_LIMITS.put(Enchantment.ARROW_FIRE, 1);
        ENCHANTMENT_LIMITS.put(Enchantment.ARROW_DAMAGE, 4);

        ROAD_LENGTHS.put(World.Environment.NORMAL, 4000);
        ROAD_LENGTHS.put(World.Environment.NETHER, 4000);

        SPAWN_RADIUS_MAP.put(World.Environment.NORMAL, 50);
        SPAWN_RADIUS_MAP.put(World.Environment.NETHER, 25);
        SPAWN_RADIUS_MAP.put(World.Environment.THE_END, 15);
    }

    public static final int SUBCLAIM_NAME_CHARACTERS_MIN = 3;
    public static final int SUBCLAIM_NAME_CHARACTERS_MAX = 16;

    public static final int FACTION_NAME_CHARACTERS_MIN = 3;
    public static final int FACTION_NAME_CHARACTERS_MAX = 16;
    public static int MAX_MEMBERS_PER_FACTION = 25;

    public static final int ROAD_DISTANCE = 2500;
    public static final int ROAD_MIN_HEIGHT = 0;   //50 'this allowed people to claim below the roads, temp disabled;
    public static final int ROAD_MAX_HEIGHT = 256; //80 'this allowed people to claim above the roads, temp disabled;

    public static final int END_PORTAL_RADIUS = 20;
    public static final int END_PORTAL_CENTER = 500;

    public static long DEFAULT_DEATHBAN_DURATION = TimeUnit.HOURS.toMillis(1L);

    // Faction tag colours.
    public static final ChatColor TEAMMATE_COLOUR = ChatColor.GREEN;
    public static final ChatColor ALLY_COLOUR = ChatColor.GOLD;
    public static final ChatColor ENEMY_COLOUR = ChatColor.RED;

    public static final ChatColor SAFEZONE_COLOUR = ChatColor.AQUA;
    public static final ChatColor ROAD_COLOUR = ChatColor.GOLD;
    public static final ChatColor WARZONE_COLOUR = ChatColor.LIGHT_PURPLE;
    public static final ChatColor WILDERNESS_COLOUR = ChatColor.DARK_GREEN;

    public static final String SCOREBOARD_TITLE = ChatColor.GREEN.toString() + ChatColor.BOLD + "    iPvP " + ChatColor.RED + "[Map " + MAP_NUMBER + "]   ";
    public static int MAX_ALLIES_PER_FACTION = 0;
    public static int MAX_CLAIMS_PER_FACTION = 8;
    public static final boolean ALLOW_CLAIMING_BESIDES_ROADS = true;

    public static final long DTR_MILLIS_BETWEEN_UPDATES = TimeUnit.SECONDS.toMillis(45L);
    public static final String DTR_WORDS_BETWEEN_UPDATES = DurationFormatUtils.formatDurationWords(DTR_MILLIS_BETWEEN_UPDATES, true, true);

    public static final double DTR_INCREMENT_BETWEEN_UPDATES = 0.1;
    public static final double MAXIMUM_DTR = 6.0;

    public static final double EXP_MULTIPLIER_GENERAL = 2.0;
    public static final double EXP_MULTIPLIER_FISHING = 2.0;
    public static final double EXP_MULTIPLIER_SMELTING = 2.0;
    public static final double EXP_MULTIPLIER_LOOTING_PER_LEVEL = 1.5;
    public static final double EXP_MULTIPLIER_LUCK_PER_LEVEL = 1.5;
    public static final double EXP_MULTIPLIER_FORTUNE_PER_LEVEL = 1.5;

    public static final int CONQUEST_POINT_LOSS_PER_DEATH = 20;
    public static final int CONQUEST_REQUIRED_WIN_POINTS = 300;

    public static final boolean FOUND_DIAMONDS_ALERTS = true;
    public static final long COMBAT_LOG_DESPAWN_TICKS = TimeUnit.SECONDS.toMillis(30L) / 50L;
    public static final boolean COMBAT_LOG_PREVENTION_ENABLED = true;
}
