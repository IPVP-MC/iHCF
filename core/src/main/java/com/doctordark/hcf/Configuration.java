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

    @Setting("factions.home.allowTeleportingInEnemyTerritory")
    private boolean allowTeleportingInEnemyTerritory = true;

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

    @Setting("bookDisenchanting")
    private boolean bookDisenchanting = true;

    @Setting("deathSigns")
    private boolean deathSigns = true;

    @Setting("deathLightning")
    private boolean deathLightning = true;

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

    @Setting("factions.home.maxHeight")
    private int maxHeightFactionHome = -1;

    @Setting("factions.home.teleportDelay.NORMAL")
    private int factionHomeTeleportDelayOverworldSeconds;
    private long factionHomeTeleportDelayOverworldMillis;

    @Setting("factions.home.teleportDelay.NETHER")
    private int factionHomeTeleportDelayNetherSeconds;
    private long factionHomeTeleportDelayNetherMillis;

    @Setting("factions.home.teleportDelay.THE_END")
    private int factionHomeTeleportDelayEndSeconds;
    private long factionHomeTeleportDelayEndMillis;

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
    private int deathbanRespawnScreenSecondsBeforeKick = 15;
    private long deathbanRespawnScreenTicksBeforeKick;

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
        int maxLevel = enchantmentLimits.get(enchantment);
        return maxLevel == enchantmentLimits.getNoEntryValue() ? enchantment.getMaxLevel() : maxLevel;
    }

    public int getPotionLimit(PotionType potionEffectType) {
        int maxLevel = potionLimits.get(potionEffectType);
        return maxLevel == potionLimits.getNoEntryValue() ? potionEffectType.getMaxLevel() : maxLevel;
    }

    protected void updateFields() {
        serverTimeZone = TimeZone.getTimeZone(serverTimeZoneName);
        serverTimeZoneID = serverTimeZone.toZoneId();
        scoreboardSidebarTitle = ChatColor.translateAlternateColorCodes('&',
                scoreboardSidebarTitle.replace("{MAP_NUMBER}", Integer.toString(mapNumber)));
        factionDtrUpdateTimeWords = DurationFormatUtils.formatDurationWords(factionDtrUpdateMillis, true, true);
        relationColourWarzone = ChatColor.valueOf(relationColourWarzoneName.replace(" ", "_").toUpperCase());
        relationColourWilderness = ChatColor.valueOf(relationColourWildernessName.replace(" ", "_").toUpperCase());
        relationColourTeammate = ChatColor.valueOf(relationColourTeammateName.replace(" ", "_").toUpperCase());
        relationColourAlly = ChatColor.valueOf(relationColourAllyName.replace(" ", "_").toUpperCase());
        relationColourEnemy = ChatColor.valueOf(relationColourEnemyName.replace(" ", "_").toUpperCase());
        relationColourRoad = ChatColor.valueOf(relationColourRoadName.replace(" ", "_").toUpperCase());
        relationColourSafezone = ChatColor.valueOf(relationColourSafezoneName.replace(" ", "_").toUpperCase());
        factionDtrRegenFreezeBaseMilliseconds = TimeUnit.MINUTES.toMillis(factionDtrRegenFreezeBaseMinutes);
        factionDtrRegenFreezeMillisecondsPerMember = TimeUnit.MINUTES.toMillis(factionDtrRegenFreezeMinutesPerMember);
        factionHomeTeleportDelayOverworldMillis = TimeUnit.SECONDS.toMillis(factionHomeTeleportDelayOverworldSeconds);
        factionHomeTeleportDelayNetherMillis = TimeUnit.SECONDS.toMillis(factionHomeTeleportDelayNetherSeconds);
        factionHomeTeleportDelayEndMillis = TimeUnit.SECONDS.toMillis(factionHomeTeleportDelayEndSeconds);
        deathbanRespawnScreenTicksBeforeKick = TimeUnit.SECONDS.toMillis(deathbanRespawnScreenSecondsBeforeKick) / 50L;

        String[] split = endExitLocationRaw.split(",");
        if (split.length == 6) {
            try {
                String worldName = split[0];
                if (Bukkit.getWorld(worldName) != null) {
                    Integer x = Integer.parseInt(split[0]);
                    Integer y = Integer.parseInt(split[1]);
                    Integer z = Integer.parseInt(split[2]);
                    Float yaw = Float.parseFloat(split[3]);
                    Float pitch = Float.parseFloat(split[3]);

                    endExitLocation = new PersistableLocation(worldName, x, y, z);
                    endExitLocation.setYaw(yaw);
                    endExitLocation.setPitch(pitch);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String splitter = " = ";
        for (String entry : potionLimitsUnstored) {
            if (entry.contains(splitter)) {
                split = entry.split(splitter);
                String key = split[0];
                Integer value = Integer.parseInt(split[1]);

                PotionType effect = PotionType.valueOf(key);
                if (effect != null) {
                    Bukkit.getLogger().log(Level.INFO, "Potion effect limit of " + effect.name() + " set as " + value);
                    potionLimits.put(effect, value);
                } else {
                    Bukkit.getLogger().log(Level.WARNING, "Unknown potion effect '" + key + "'.");
                }
            }
        }

        for (String entry : enchantmentLimitsUnstored) {
            if (entry.contains(splitter)) {
                split = entry.split(splitter);
                String key = split[0];
                Integer value = Integer.parseInt(split[1]);

                Enchantment enchantment = Enchantment.getByName(key);
                if (enchantment != null) {
                    Bukkit.getLogger().log(Level.INFO, "Enchantment limit of " + enchantment.getName() + " set as " + value);
                    enchantmentLimits.put(enchantment, value);
                } else {
                    Bukkit.getLogger().log(Level.WARNING, "Unknown enchantment effect '" + key + "'.");
                }
            }
        }
    }
}