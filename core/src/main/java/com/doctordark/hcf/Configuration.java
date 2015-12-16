package com.doctordark.hcf;

import com.doctordark.util.PersistableLocation;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter
public class Configuration extends AnnotationConfig {

    @Setting("handleEntityLimiting")
    private boolean handleEntityLimiting = true;

    @Setting("removeInfinityArrowsOnLand")
    private boolean removeInfinityArrowsOnLand = true;

    @Setting("beaconStrengthLevelLimit")
    private int beaconStrengthLevelLimit = 1;

    @Setting("disableBoatPlacementOnLand")
    private boolean disableBoatPlacementOnLand = true;

    @Setting("enderpearlGlitching.enabled")
    private boolean enderpearlGlitchingEnabled = true;

    @Setting("enderpearlGlitching.refund")
    private boolean enderpearlGlitchingRefund = true;

    @Setting("disableEnderchests")
    private boolean disableEnderchests = true;

    @Setting("preventPlacingBedsNether")
    private boolean preventPlacingBedsNether = false;

    @Getter(AccessLevel.NONE)
    @Setting("serverTimeZone")
    private String serverTimeZoneName = "EST";
    private TimeZone serverTimeZone;
    private ZoneId serverTimeZoneID;

    @Setting("furnaceCookSpeedMultiplier")
    private float furnaceCookSpeedMultiplier = 6.0F;

    @Setting("bottledExp")
    private boolean bottledExp = true;

    @Setting("bookDeenchanting")
    private boolean bookDeenchanting = true;

    @Setting("deathSigns")
    private boolean deathSigns = true;

    @Setting("deathLightning")
    private boolean deathLightning = true;

    @Setting("mapNumber")
    private int mapNumber = 1;

    @Setting("kitMap")
    private boolean kitMap = false;

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

    @Setting("scoreboard.sidebar.title")
    private String scoreboardSidebarTitle = "&a&lHCF &c[Map {MAP_NUMBER}]";

    @Setting("scoreboard.sidebar.enabled")
    private boolean scoreboardSidebarEnabled = true;

    @Setting("scoreboard.nametags.enabled")
    private boolean scoreboardNametagsEnabled = true;

    @Setting("combatlog.enabled")
    private boolean handleCombatLogging = true;

    @Setting("combatlog.despawnDelayTicks")
    private int combatlogDespawnDelayTicks = 600;

    @Setting("warzone.radiusOverworld")
    private int warzoneRadiusOverworld = 800;

    @Setting("warzone.radiusNether")
    private int warzoneRadiusNether = 800;

    @Setting("factions.conquest.pointLossPerDeath")
    private int conquestPointLossPerDeath = 20;

    @Setting("factions.conquest.requiredVictoryPoints")
    private int conquestRequiredVictoryPoints = 300;

    @Setting("factions.conquest.allowNegativePoints")
    private boolean conquestAllowNegativePoints = true;

    @Setting("factions.roads.allowClaimsBesides")
    private boolean allowClaimsBesidesRoads = true;

    //TODO: CaseInsensitiveList
    @Setting("factions.disallowedFactionNames")
    private List<String> factionDisallowedNames = new ArrayList<>();

    @Setting("factions.maxHomeHeight")
    private int maxHeightFactionHome = -1;

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

    @Setting("factions.subclaim.nameMinCharacters")
    private int factionSubclaimNameMinCharacters = 3;

    @Setting("factions.subclaim.nameMaxCharacters")
    private int factionSubclaimNameMaxCharacters = 16;

    @Getter(AccessLevel.NONE)
    @Setting("factions.dtr.regenFreeze.baseMinutes")
    private int factionDtrRegenFreezeBaseMinutes = 40;
    private long factionDtrRegenFreezeBaseMilliseconds;

    @Getter(AccessLevel.NONE)
    @Setting("factions.dtr.regenFreeze.minutesPerMember")
    private int factionDtrRegenFreezeMinutesPerMember = 2;
    private long factionDtrRegenFreezeMillisecondsPerMember;

    @Setting("factions.dtr.minimum")
    private int factionMinimumDtr = -50;

    @Setting("factions.dtr.maximum")
    private float factionMaximumDtr = 6.0F;

    @Setting("factions.dtr.millisecondsBetweenUpdates")
    private int factionDtrUpdateMillis = 45000; // 45 seconds
    private String factionDtrUpdateTimeWords;

    @Setting("factions.dtr.incrementBetweenUpdates")
    private float factionDtrUpdateIncrement = 0.1F;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.warzone")
    private String relationColourWarzoneName = "LIGHT_PURPLE";
    private ChatColor relationColourWarzone = ChatColor.LIGHT_PURPLE;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.wilderness")
    private String relationColourWildernessName = "DARK_GREEN";
    private ChatColor relationColourWilderness = ChatColor.DARK_GREEN;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.teammate")
    private String relationColourTeammateName = "GREEN";
    private ChatColor relationColourTeammate = ChatColor.GREEN;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.ally")
    private String relationColourAllyName = "GOLD";
    private ChatColor relationColourAlly = ChatColor.GOLD;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.enemy")
    private String relationColourEnemyName = "RED";
    private ChatColor relationColourEnemy = ChatColor.RED;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.road")
    private String relationColourRoadName = "YELLOW";
    private ChatColor relationColourRoad = ChatColor.YELLOW;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.safezone")
    private String relationColourSafezoneName = "AQUA";
    private ChatColor relationColourSafezone = ChatColor.AQUA;

    @Setter
    @Setting("deathban.baseDurationMinutes")
    private int deathbanBaseDurationMinutes = 60;

    @Setter
    @Setting("deathban.respawnScreenSecondsBeforeKick")
    private int respawnScreenSecondsBeforeKick = 15;

    @Setting("end.open")
    private boolean endOpen = true;

    @Setting("end.exitLocation")
    private String endExitLocationRaw = "world,0.5,75,0.5,0,0";
    private PersistableLocation endExitLocation = new PersistableLocation(Bukkit.getWorld("world"), 0.5, 75, 0.5);

    @Setting("end.extinguishFireOnExit")
    private boolean endExtinguishFireOnExit = true;

    @Setting("end.removeStrengthOnEntrance")
    private boolean endRemoveStrengthOnEntrance = true;

    @Setting("eotw.chatSymbolPrefix")
    private String eotwChatSymbolPrefix = " \u2605";

    @Setting("eotw.chatSymbolSuffix")
    private String eotwChatSymbolSuffix = "";

    //TODO: UUID list not UUID string list
    @Setting("eotw.lastMapCapperUuids")
    private List<String> eotwLastMapCapperUuids = new ArrayList<>();

    @SuppressWarnings("ALL")
    @Setting("potionLimits")
    private final List<String> potionLimitsUnstored = new ArrayList<>();

    @SuppressWarnings("ALL")
    @Setting("enchantmentLimits")
    private final List<String> enchantmentLimitsUnstored = new ArrayList<>();

    @Setting("subclaimSigns.private")
    private boolean subclaimSignPrivate = false;

    @Setting("subclaimSigns.captain")
    private boolean subclaimSignCaptain = false;

    @Setting("subclaimSigns.leader")
    private boolean subclaimSignLeader = false;

    @Setting("subclaimSigns.hopperCheck")
    private boolean subclaimHopperCheck = false;

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

    protected void updateFields() {
        this.serverTimeZone = TimeZone.getTimeZone(this.serverTimeZoneName);
        this.serverTimeZoneID = this.serverTimeZone.toZoneId();
        this.scoreboardSidebarTitle = ChatColor.translateAlternateColorCodes('&', this.scoreboardSidebarTitle.replace("{MAP_NUMBER}", Integer.toString(this.mapNumber)));
        this.factionDtrUpdateTimeWords = DurationFormatUtils.formatDurationWords(this.factionDtrUpdateMillis, true, true);
        this.relationColourWarzone = ChatColor.valueOf(this.relationColourWarzoneName.replace(" ", "_").toUpperCase());
        this.relationColourWilderness = ChatColor.valueOf(this.relationColourWildernessName.replace(" ", "_").toUpperCase());
        this.relationColourTeammate = ChatColor.valueOf(this.relationColourTeammateName.replace(" ", "_").toUpperCase());
        this.relationColourAlly = ChatColor.valueOf(this.relationColourAllyName.replace(" ", "_").toUpperCase());
        this.relationColourEnemy = ChatColor.valueOf(this.relationColourEnemyName.replace(" ", "_").toUpperCase());
        this.relationColourRoad = ChatColor.valueOf(this.relationColourRoadName.replace(" ", "_").toUpperCase());
        this.relationColourSafezone = ChatColor.valueOf(this.relationColourSafezoneName.replace(" ", "_").toUpperCase());
        this.factionDtrRegenFreezeBaseMilliseconds = TimeUnit.MINUTES.convert(this.factionDtrRegenFreezeBaseMinutes, TimeUnit.MILLISECONDS);
        this.factionDtrRegenFreezeMillisecondsPerMember = TimeUnit.MINUTES.convert(this.factionDtrRegenFreezeMinutesPerMember, TimeUnit.MILLISECONDS);

        String[] split = this.endExitLocationRaw.split(",");
        if (split.length == 6) {
            try {
                String worldName = split[0];
                if (Bukkit.getWorld(worldName) != null) {
                    Integer x = Integer.parseInt(split[0]);
                    Integer y = Integer.parseInt(split[1]);
                    Integer z = Integer.parseInt(split[2]);
                    Float yaw = Float.parseFloat(split[3]);
                    Float pitch = Float.parseFloat(split[3]);

                    this.endExitLocation = new PersistableLocation(worldName, x, y, z);
                    this.endExitLocation.setYaw(yaw);
                    this.endExitLocation.setPitch(pitch);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }


        String splitter = " = ";
        for (String entry : this.potionLimitsUnstored) {
            if (entry.contains(splitter)) {
                split = entry.split(splitter);
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
                split = entry.split(splitter);
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