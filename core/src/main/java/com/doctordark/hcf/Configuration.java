package com.doctordark.hcf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.gnu.trove.impl.Constants;
import net.minecraft.util.gnu.trove.map.TObjectIntMap;
import net.minecraft.util.gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.org.apache.commons.lang3.time.DurationFormatUtils;
import net.techcable.techutils.config.AnnotationConfig;
import net.techcable.techutils.config.Setting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

@Getter
public class Configuration extends AnnotationConfig {

    @Setter
    @Setting("deathban.baseDurationMinutes")
    private int deathbanBaseDurationMinutes = 60;

    @Setting("maxHeightFactionHome")
    private int maxHeightFactionHome = -1;

    @Setting("end.open")
    private boolean endOpen = true;

    @Setting("end.extinguishFireOnExit")
    private boolean endExtinguishFireOnExit = true;

    @Setting("end.removeStrengthOnEntrance")
    private boolean endRemoveStrengthOnEntrance = true;

    @Setting("furnaceCookSpeedMultiplier")
    private float furnaceCookSpeedMultiplier = 6.0F;

    @Setting("bottledExp")
    private boolean bottledExp = true;

    @Setting("bookDeenchanting")
    private boolean bookDeenchanting = true;

    @Setting("eotw.chatSymbolPrefix")
    private String eotwChatSymbolPrefix = " \u2605";

    @Setting("eotw.chatSymbolSuffix")
    private String eotwChatSymbolSuffix = "";

    //TODO: UUID list not UUID string list
    @Setting("eotw.lastMapCapperUuids")
    private List<String> eotwLastMapCapperUuids = new ArrayList<>();

    @Setting("deathSigns")
    private boolean deathSigns = true;

    @Setting("deathLightning")
    private boolean deathLightning = true;

    @Setting("kitMap")
    private boolean kitMap = false;

    @Setting("mapNumber")
    private int mapNumber = 1;

    @Setting("preventAllyDamage")
    private boolean preventAllyAttackDamage = true;

    @Setting("economy.startingBalance")
    private int economyStartingBalance = 250;

    @Setting("spawners.preventBreakingNether")
    private boolean spawnersPreventBreakingNether = true;

    @Setting("spawners.preventPlacingNether")
    private boolean spawnersPreventPlacingNether = true;

    @Setting("expMultiplier.global")
    private float expMultiplierGlobal = 1.0F;

    @Setting("expMultiplier.fishing")
    private float expMultiplierFishing = 1.0F;

    @Setting("expMultiplier.smelting")
    private float expMultiplierSmelting = 1.0F;

    @Setting("expMultiplier.lootingPerLevel")
    private float expMultiplierLootingPerLevel = 1.0F;

    @Setting("expMultiplier.luckPerLevel")
    private float expMultiplierLuckPerLevel = 1.0F;

    @Setting("expMultiplier.fortunePerLevel")
    private float expMultiplierFortunePerLevel = 1.0F;

    @Setting("roads.allowClaimsBesides")
    private boolean allowClaimsBesidesRoads = true;

    @Setting("scoreboard.sidebar.title")
    private String scoreboardSidebarTitle = "&a&lHCF &c[Map {MAP_NUMBER}]";

    @Setting("scoreboard.sidebar.enabled")
    private boolean scoreboardSidebarEnabled = true;

    @Setting("scoreboard.nametags.enabled")
    private boolean scoreboardNametagsEnabled = true;

    @Setting("combatlog.enabled")
    private boolean combatlogEnabled = true;

    @Setting("combatlog.despawnDelayTicks")
    private int combatlogDespawnDelayTicks = 600;

    @Setting("conquest.pointLossPerDeath")
    private int conquestPointLossPerDeath = 20;

    @Setting("conquest.requiredVictoryPoints")
    private int conquestRequiredVictoryPoints = 300;

    @Setting("conquest.allowNegativePoints")
    private boolean conquestAllowNegativePoints = true;

    @Setting("warzone.radius")
    private int warzoneRadius = 850;

    //TODO: CaseInsensitiveList
    @Setting("factions.disallowedFactionNames")
    private List<String> factionDisallowedNames = new ArrayList<>();

    @Setting("factions.nameMinCharacters")
    private int factionNameMinCharacters = 3;

    @Setting("factions.nameMaxCharacters")
    private int factionNameMaxCharacters = 16;

    @Setting("factions.maxMembers")
    private int factionMaxMembers = 25;

    @Setting("factions.maxClaims")
    private int factionMaxClaims = 8;

    @Setting("factions.maxAllies")
    private int factionMaxAllies = 1;

    @Setting("factions.dtr.minimum")
    private int factionMinimumDtr = -50;

    @Setting("factions.dtr.maximum")
    private int factionMaximumDtr = 6;

    @Setting("factions.dtr.millisecondsBetweenUpdates")
    private int factionDtrUpdateMillis = 45000;
    private String factionDtrUpdateTimeWords;

    @Setting("factions.dtr.incrementBetweenUpdates")
    private float factionDtrUpdateIncrement = 0.1F;

    @Getter(AccessLevel.NONE)
    @Setting("relationColours.wilderness")
    private String relationColourWildernessName = "DARK_GREEN";
    private ChatColor relationColourWilderness = ChatColor.DARK_GREEN;

    @Getter(AccessLevel.NONE)
    @Setting("relationColours.warzone")
    private String relationColourWarzoneName = "LIGHT_PURPLE";
    private ChatColor relationColourWarzone = ChatColor.LIGHT_PURPLE;

    @Getter(AccessLevel.NONE)
    @Setting("relationColours.teammate")
    private String relationColourTeammateName = "GREEN";
    private ChatColor relationColourTeammate = ChatColor.GREEN;

    @Getter(AccessLevel.NONE)
    @Setting("relationColours.ally")
    private String relationColourAllyName = "GOLD";
    private ChatColor relationColourAlly = ChatColor.GOLD;

    @Getter(AccessLevel.NONE)
    @Setting("relationColours.enemy")
    private String relationColourAllyEnemy = "RED";
    private ChatColor relationColourEnemy = ChatColor.RED;

    @Getter(AccessLevel.NONE)
    @Setting("relationColours.road")
    private String relationColourRoadName = "YELLOW";
    private ChatColor relationColourRoad = ChatColor.YELLOW;

    @Getter(AccessLevel.NONE)
    @Setting("relationColours.safezone")
    private String relationColourSafezoneName = "AQUA";
    private ChatColor relationColourSafezone = ChatColor.AQUA;

    @Getter(AccessLevel.NONE)
    @Setting("serverTimeZone")
    private String serverTimeZoneName = "EST";

    private TimeZone serverTimeZone;
    private ZoneId serverTimeZoneID;

    @SuppressWarnings("ALL")
    @Setting("potionLimits")
    private final List<String> potionLimitsUnstored = new ArrayList<>();

    @SuppressWarnings("ALL")
    @Setting("enchantmentLimits")
    private final List<String> enchantmentLimitsUnstored = new ArrayList<>();

    private final TObjectIntMap<Enchantment> enchantmentLimits = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
    private final TObjectIntMap<PotionType> potionLimits = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);

    public int getEnchantmentLimit(Enchantment enchantment) {
        int maxLevel = this.enchantmentLimits.get(enchantment);
        return maxLevel == -1 ? enchantment.getMaxLevel() : maxLevel;
    }

    public int getPotionLimit(PotionType potionEffectType) {
        int maxLevel = this.potionLimits.get(potionEffectType);
        return maxLevel == -1 ? potionEffectType.getMaxLevel() : maxLevel;
    }

    @Override
    public void load(File configFile, URL defaultConfigUrl) throws IOException, InvalidConfigurationException {
        super.load(configFile, defaultConfigUrl);

        this.scoreboardSidebarTitle = ChatColor.translateAlternateColorCodes('&', this.scoreboardSidebarTitle.replace("{MAP_NUMBER}", Integer.toString(this.mapNumber)));
        this.factionDtrUpdateTimeWords = DurationFormatUtils.formatDurationWords(this.factionDtrUpdateMillis, true, true);
        this.relationColourWarzone = ChatColor.valueOf(this.relationColourWarzoneName.replace(" ", "_").toUpperCase());
        this.relationColourWilderness = ChatColor.valueOf(this.relationColourWildernessName.replace(" ", "_").toUpperCase());

        this.serverTimeZone = TimeZone.getTimeZone(this.serverTimeZoneName);
        this.serverTimeZoneID = this.serverTimeZone.toZoneId();

        String splitter = " = ";
        for (String entry : this.potionLimitsUnstored) {
            if (entry.contains(splitter)) {
                String[] split = entry.split(splitter);
                String key = split[0];
                Integer value = Integer.parseInt(split[1]);

                PotionType effect = PotionType.valueOf(key);
                if (effect != null) {
                    Bukkit.getLogger().log(Level.INFO, "Potion effect limit of " + effect.name() + " set as " + value);
                    this.potionLimits.put(effect, value);
                } else {
                    Bukkit.getLogger().log(Level.WARNING, "Unknown potion effect '" + key + "'.");
                }
            }
        }

        for (String entry : this.enchantmentLimitsUnstored) {
            if (entry.contains(splitter)) {
                String[] split = entry.split(splitter);
                String key = split[0];
                Integer value = Integer.parseInt(split[1]);

                Enchantment enchantment = Enchantment.getByName(key);
                if (enchantment != null) {
                    Bukkit.getLogger().log(Level.INFO, "Enchantment limit of " + enchantment.getName() + " set as " + value);
                    this.enchantmentLimits.put(enchantment, value);
                } else {
                    Bukkit.getLogger().log(Level.WARNING, "Unknown enchantment effect '" + key + "'.");
                }
            }
        }
    }
}