package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.faction.CapturableFaction;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.event.CaptureZoneEnterEvent;
import com.doctordark.hcf.faction.event.CaptureZoneLeaveEvent;
import com.doctordark.hcf.faction.event.PlayerClaimEnterEvent;
import com.doctordark.hcf.faction.struct.Raidable;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.ClaimableFaction;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.hcf.faction.type.WarzoneFaction;
import com.doctordark.util.BukkitUtils;
import com.doctordark.util.cuboid.Cuboid;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.material.Cauldron;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;

/**
 * Listener that manages protection for {@link Claim}s.
 */
public class ProtectionListener implements Listener {

    public static final String PROTECTION_BYPASS_PERMISSION = "hcf.faction.protection.bypass";

    // List of materials a player can not right click in enemy territory. ~No such ImmutableEnumMultimap in current Guava build :/
    private static final ImmutableMultimap<Material, Material> ITEM_ON_BLOCK_RIGHT_CLICK_DENY = ImmutableMultimap.<Material, Material>builder().
            put(Material.DIAMOND_HOE, Material.GRASS).
            put(Material.GOLD_HOE, Material.GRASS).
            put(Material.IRON_HOE, Material.GRASS).
            put(Material.STONE_HOE, Material.GRASS).
            put(Material.WOOD_HOE, Material.GRASS).
            build();

    // List of materials a player can not right click in enemy territory.
    private static final ImmutableSet<Material> BLOCK_RIGHT_CLICK_DENY = Sets.immutableEnumSet(
            Material.BED,
            Material.BED_BLOCK,
            Material.BEACON,
            Material.FENCE_GATE,
            Material.IRON_DOOR,
            Material.TRAP_DOOR,
            Material.WOOD_DOOR,
            Material.WOODEN_DOOR,
            Material.IRON_DOOR_BLOCK,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.FURNACE,
            Material.BURNING_FURNACE,
            Material.BREWING_STAND,
            Material.HOPPER,
            Material.DROPPER,
            Material.DISPENSER,
            Material.STONE_BUTTON,
            Material.WOOD_BUTTON,
            Material.ENCHANTMENT_TABLE,
            Material.WORKBENCH,
            Material.ANVIL,
            Material.LEVER,
            Material.FIRE
    );

    private final HCF plugin;

    public ProtectionListener(HCF plugin) {
        this.plugin = plugin;
    }

    private void handleMove(PlayerMoveEvent event, PlayerClaimEnterEvent.EnterCause enterCause) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        boolean cancelled = false;

        Faction fromFaction = plugin.getFactionManager().getFactionAt(from);
        Faction toFaction = plugin.getFactionManager().getFactionAt(to);
        if (fromFaction != toFaction) {
            PlayerClaimEnterEvent calledEvent = new PlayerClaimEnterEvent(player, from, to, fromFaction, toFaction, enterCause);
            Bukkit.getPluginManager().callEvent(calledEvent);
            cancelled = calledEvent.isCancelled();
        } else if (toFaction instanceof CapturableFaction) {
            CapturableFaction capturableFaction = (CapturableFaction) toFaction;
            for (CaptureZone captureZone : capturableFaction.getCaptureZones()) {
                Cuboid cuboid = captureZone.getCuboid();
                if (cuboid == null) {
                    continue;
                }

                if (cuboid.contains(from)) {
                    if (!cuboid.contains(to)) {
                        CaptureZoneLeaveEvent calledEvent = new CaptureZoneLeaveEvent(player, capturableFaction, captureZone);
                        Bukkit.getPluginManager().callEvent(calledEvent);
                        cancelled = calledEvent.isCancelled();
                        break;
                    }
                } else {
                    if (cuboid.contains(to)) {
                        CaptureZoneEnterEvent calledEvent = new CaptureZoneEnterEvent(player, capturableFaction, captureZone);
                        Bukkit.getPluginManager().callEvent(calledEvent);
                        cancelled = calledEvent.isCancelled();
                        break;
                    }
                }
            }
        }

        if (cancelled) {
            if (enterCause == PlayerClaimEnterEvent.EnterCause.TELEPORT) {
                event.setCancelled(true);
            } else {
                from.setX(from.getBlockX() + 0.5);
                from.setZ(from.getBlockZ() + 0.5);
                event.setTo(from);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        this.handleMove(event, PlayerClaimEnterEvent.EnterCause.MOVEMENT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerTeleportEvent event) {
        this.handleMove(event, PlayerClaimEnterEvent.EnterCause.TELEPORT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        switch (event.getCause()) {
            case FLINT_AND_STEEL:
            case ENDER_CRYSTAL:
                return;
        }

        Faction factionAt = plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    // Original source by mFactions: https://github.com/MassiveCraft/Factions/blob/dab81ede383aeb76606daf5a3c859775e1b3778/src/com/massivecraft/factions/engine/EngineExploit.java
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onStickyPistonExtend(BlockPistonExtendEvent event) {
        Block block = event.getBlock();

        // Targets end-of-the-line empty (AIR) block which is being pushed into, including if piston itself would extend into air.
        Block targetBlock = block.getRelative(event.getDirection(), event.getLength() + 1);
        if (targetBlock.isEmpty() || targetBlock.isLiquid()) { // If potentially pushing into AIR/WATER/LAVA in another territory, check it out.
            Faction targetFaction = plugin.getFactionManager().getFactionAt(targetBlock.getLocation());
            if (targetFaction instanceof Raidable && !((Raidable) targetFaction).isRaidable() && targetFaction != plugin.getFactionManager().getFactionAt(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onStickyPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky()) return; // If not a sticky piston, retraction should be fine.

        // If potentially retracted block is just AIR/WATER/LAVA, no worries
        Location retractLocation = event.getRetractLocation();
        Block retractBlock = retractLocation.getBlock();
        if (!retractBlock.isEmpty() && !retractBlock.isLiquid()) {
            Block block = event.getBlock();
            Faction targetFaction = plugin.getFactionManager().getFactionAt(retractLocation);
            if (targetFaction instanceof Raidable && !((Raidable) targetFaction).isRaidable() && targetFaction != plugin.getFactionManager().getFactionAt(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block fromBlock = event.getBlock();
        Material fromType = fromBlock.getType();
        if (fromType == Material.WATER || fromType == Material.STATIONARY_WATER || fromType == Material.LAVA || fromType == Material.STATIONARY_LAVA) {
            if (!ProtectionListener.canBuildAt(fromBlock.getLocation(), event.getToBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Faction toFactionAt = plugin.getFactionManager().getFactionAt(event.getTo());
            if (toFactionAt.isSafezone() && !plugin.getFactionManager().getFactionAt(event.getFrom()).isSafezone()) {
                Player player = event.getPlayer();
                player.sendMessage(ChatColor.RED + "You cannot Enderpearl into safe-zones, used Enderpearl has been refunded.");
                plugin.getTimerManager().getEnderPearlTimer().refund(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            Location from = event.getFrom();
            Location to = event.getTo();
            Player player = event.getPlayer();

            Faction fromFac = plugin.getFactionManager().getFactionAt(from);
            if (fromFac.isSafezone()) { // teleport player to spawn point of target if came from safe-zone.
                event.setTo(to.getWorld().getSpawnLocation().add(0.5, 0, 0.5));
                event.useTravelAgent(false);
                player.sendMessage(ChatColor.YELLOW + "You were teleported to the spawn of target world as you were in a safe-zone.");
                return;
            }

            if (event.useTravelAgent() && to.getWorld().getEnvironment() == World.Environment.NORMAL) {
                TravelAgent travelAgent = event.getPortalTravelAgent();
                if (!travelAgent.getCanCreatePortal()) return;

                Location foundPortal = travelAgent.findPortal(to);
                if (foundPortal != null) return; // there is already an exit portal, so ignore

                Faction factionAt = plugin.getFactionManager().getFactionAt(to);
                if (factionAt instanceof ClaimableFaction) {
                    Faction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                    if (playerFaction != factionAt) {
                        player.sendMessage(ChatColor.YELLOW + "Portal would have created portal in territory of " + factionAt.getDisplayName(player) + ChatColor.YELLOW + '.');
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    // Prevent mobs from spawning in the Warzone, safe-zones or claims.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) { // allow slimes to always split
            return;
        }

        Location location = event.getLocation();
        Faction factionAt = plugin.getFactionManager().getFactionAt(location);
        if (factionAt.isSafezone() && reason == CreatureSpawnEvent.SpawnReason.SPAWNER) { // allow creatures to spawn in safe-zones by Spawner
            return;
        }

        if (factionAt instanceof ClaimableFaction && ((!(factionAt instanceof Raidable) || !((Raidable) factionAt).isRaidable())) && event.getEntity() instanceof Monster) {
            switch (reason) {
                case SPAWNER:
                case EGG:
                case CUSTOM:
                case BUILD_WITHER:
                case BUILD_IRONGOLEM:
                case BUILD_SNOWMAN:
                    return;
                default:
                    event.setCancelled(true);
            }
        }
    }

    // Prevents players attacking or taking damage when in safe-zone protected areas.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            Faction playerFactionAt = plugin.getFactionManager().getFactionAt(player.getLocation());
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (playerFactionAt.isSafezone() && cause != EntityDamageEvent.DamageCause.SUICIDE && cause != EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
            }

            Player attacker = BukkitUtils.getFinalAttacker(event, true);
            if (attacker != null) {
                Faction attackerFactionAt = plugin.getFactionManager().getFactionAt(attacker.getLocation());
                if (attackerFactionAt.isSafezone()) {
                    event.setCancelled(true);
                    attacker.sendMessage(ChatColor.RED + "You cannot attack players whilst in safe-zones.");
                    return;
                } else if (playerFactionAt.isSafezone()) {
                    // it's already cancelled above.
                    attacker.sendMessage(ChatColor.RED + "You cannot attack players that are in safe-zones.");
                    return;
                }

                PlayerFaction attackerFaction;
                PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                if (playerFaction != null && ((attackerFaction = plugin.getFactionManager().getPlayerFaction(attacker)) != null)) {
                    Role role = playerFaction.getMember(player).getRole();
                    String hiddenAstrixedName = role.getAstrix() + (player.hasPotionEffect(PotionEffectType.INVISIBILITY) ? "???" : player.getName());
                    if (attackerFaction == playerFaction) {
                        attacker.sendMessage(plugin.getConfiguration().getRelationColourTeammate() + hiddenAstrixedName + ChatColor.YELLOW + " is in your faction.");
                        event.setCancelled(true);
                    } else if (attackerFaction.getAllied().contains(playerFaction.getUniqueID())) {
                        ChatColor color = plugin.getConfiguration().getRelationColourAlly();
                        // if (plugin.getConfiguration().isPreventAllyAttackDamage()) {
                        //    event.setCancelled(true);
                        //    attacker.sendMessage(color + hiddenAstrixedName + ChatColor.YELLOW + " is an ally.");
                        // } else {
                        attacker.sendMessage(ChatColor.YELLOW + "Careful! " + color + hiddenAstrixedName + ChatColor.YELLOW + " is an ally.");
                        // }
                    }
                }
            }
        }
    }

    // Prevents losing hunger in safe-zones.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player && ((Player) entity).getFoodLevel() > event.getFoodLevel() && plugin.getFactionManager().getFactionAt(entity.getLocation()).isSafezone()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getEntity();
        if (!BukkitUtils.isDebuff(potion)) {
            return;
        }

        // Prevents potion effecting players that are in safe-zones.
        Faction factionAt = plugin.getFactionManager().getFactionAt(potion.getLocation());
        if (factionAt.isSafezone()) {
            event.setCancelled(true);
            return;
        }

        ProjectileSource source = potion.getShooter();
        if (source instanceof Player) {
            Player player = (Player) source;
            //Allow faction members to splash damage their own, PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            for (LivingEntity affected : event.getAffectedEntities()) {
                if (affected instanceof Player && !player.equals(affected)) {
                    Player target = (Player) affected;
                    if (target.equals(source)) continue; // allow the source to be affected regardless
                    if (plugin.getFactionManager().getFactionAt(target.getLocation()).isSafezone()/*Nope || playerFaction.getMembers().containsKey(other.getUniqueId())*/) {
                        event.setIntensity(affected, 0);
                    }
                }
            }
        }
    }

    // Prevent monsters targeting players in safe-zones or their own claims.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {
        switch (event.getReason()) {
            case CLOSEST_PLAYER:
            case RANDOM_TARGET:
                Entity target = event.getTarget();
                if (event.getEntity() instanceof LivingEntity && target instanceof Player) {
                    // Check LivingEntity instance, things like experience orbs might lag spam ;/
                    Faction playerFaction; // lazy-load
                    Faction factionAt = plugin.getFactionManager().getFactionAt(target.getLocation());
                    if (factionAt.isSafezone() || ((playerFaction = plugin.getFactionManager().getPlayerFaction((Player) target)) != null && factionAt == playerFaction)) {
                        event.setCancelled(true);
                    }
                }
                break;
            default:
                break;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }

        Block block = event.getClickedBlock();
        Action action = event.getAction();
        if (action == Action.PHYSICAL) { // Prevent players from trampling on crops or pressure plates, etc.
            if (!attemptBuild(event.getPlayer(), block.getLocation(), null)) {
                event.setCancelled(true);
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            boolean canRightClick;
            MaterialData blockData;
            Material blockType = block.getType();

            // Firstly, check if this block is not on the explicit blacklist.
            canRightClick = !BLOCK_RIGHT_CLICK_DENY.contains(blockType);
            if (canRightClick) {
                Material itemType = event.hasItem() ? event.getItem().getType() : null;

                if (Material.EYE_OF_ENDER == itemType && Material.ENDER_PORTAL_FRAME == blockType && block.getData() != 4) {
                    // If the player is right clicking an Ender Portal Frame with an Ender Portal Eye and it is empty.
                    canRightClick = false;

                } else if (Material.GLASS_BOTTLE == itemType && (blockData = block.getState().getData()) instanceof Cauldron && !((Cauldron) blockData).isEmpty()) {
                    // If the player is right clicking a Cauldron that contains liquid with a Glass Bottle.
                    canRightClick = false;

                } else if (itemType != null && ITEM_ON_BLOCK_RIGHT_CLICK_DENY.get(itemType).contains(block.getType())) {
                    // Finally, check if this block is not blacklisted with the item the player right clicked it with.
                    canRightClick = false;

                }
            } else if (block.getType() == Material.WORKBENCH && plugin.getFactionManager().getFactionAt(block.getLocation()).isSafezone()) {
                // Allow workbench use in safezones.
                canRightClick = true;
            }

            if (!canRightClick && !attemptBuild(event.getPlayer(), block.getLocation(), ChatColor.YELLOW + "You cannot do this in the territory of %1$s" + ChatColor.YELLOW + '.', true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBurn(BlockBurnEvent event) {
        Faction factionAt = plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof WarzoneFaction || (factionAt instanceof Raidable && !((Raidable) factionAt).isRaidable())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent event) {
        Faction factionAt = plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    /*@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent event) {
        Faction factionAt = plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }*/

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLeavesDelay(LeavesDecayEvent event) {
        Faction factionAt = plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockForm(BlockFormEvent event) {
        Faction factionAt = plugin.getFactionManager().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity && !attemptBuild(entity, event.getBlock().getLocation(), null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getBlock().getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getBlockPlaced().getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getBlockClicked().getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getBlockClicked().getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (remover instanceof Player) {
            if (!attemptBuild(remover, event.getEntity().getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getEntity().getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onVehicleEnter(VehicleEnterEvent event) {
        Entity entered = event.getEntered();
        if (entered instanceof Player) {
            Vehicle vehicle = event.getVehicle();
            if (!attemptBuild(event.getEntered(), vehicle.getLocation(), ChatColor.YELLOW + "You cannot enter vehicles in the territory of %1$s" + ChatColor.YELLOW + '.')) {
                event.setCancelled(true);
                return;
            }

            // Prevent players using horses that don't belong to them.
            if (vehicle instanceof Horse) {
                Horse horse = (Horse) event.getVehicle();
                AnimalTamer owner = horse.getOwner();
                if (owner != null && !owner.equals(entered)) {
                    ((Player) entered).sendMessage(ChatColor.YELLOW + "You cannot enter a Horse that belongs to " + ChatColor.RED + owner.getName() + ChatColor.YELLOW + '.');
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Player damager = null;
        Entity attacker = event.getAttacker();
        if (attacker instanceof Player) {
            damager = (Player) attacker;
        } else if (attacker instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) attacker).getShooter();
            if (shooter instanceof Player) {
                damager = (Player) shooter;
            }
        }

        if (damager != null && !attemptBuild(attacker, damager.getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
            event.setCancelled(true);
        }
    }

    // Prevents items that are in Item Frames OR hanging entities (PAINTINGS, etc) being removed.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Hanging) {
            Player attacker = BukkitUtils.getFinalAttacker(event, false);
            if (attacker != null && !attemptBuild(attacker, entity.getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents items that are in Item Frames being rotated.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onHangingInteractByPlayer(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Hanging) {
            if (!attemptBuild(event.getPlayer(), entity.getLocation(), ChatColor.YELLOW + "You cannot build in the territory of %1$s" + ChatColor.YELLOW + '.')) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Checks if a entity is eligible to build at a given location, if not
     * it will send the deny message passed in the constructor.
     * <p>The deny message will be formatted using {@link String#format(String, Object...)}</p>
     * <p>The first formatted argument is the display name of the enemy faction to the player</p>
     *
     * @param entity      the entity to attempt for
     * @param location    the location to attempt at
     * @param denyMessage the deny message to send
     * @return true if the player can build at location
     */
    public static boolean attemptBuild(Entity entity, Location location, @Nullable String denyMessage) {
        return attemptBuild(entity, location, denyMessage, false);
    }

    /**
     * Checks if a entity is eligible to build at a given location, if not
     * it will send the deny message passed in the constructor.
     * <p>The deny message will be formatted using {@link String#format(String, Object...)}</p>
     * <p>The first formatted argument is the display name of the enemy faction to the player</p>
     *
     * @param entity        the entity to attempt for
     * @param location      the location to attempt at
     * @param denyMessage   the deny message to send
     * @param isInteraction if the entity is trying to interact
     * @return true if the player can build at location
     */
    public static boolean attemptBuild(Entity entity, Location location, @Nullable String denyMessage, boolean isInteraction) {
        Player player = entity instanceof Player ? (Player) entity : null;

        // Allow CREATIVE players with specified permission to bypass this protection.
        if (player != null && player.getGameMode() == GameMode.CREATIVE && player.hasPermission(PROTECTION_BYPASS_PERMISSION)) {
            return true;
        }

        if (player != null && player.getWorld().getEnvironment() == World.Environment.THE_END) {
            player.sendMessage(ChatColor.RED + "You cannot build in the end.");
            return false;
        }

        boolean result = false;
        Faction factionAt = HCF.getPlugin().getFactionManager().getFactionAt(location);
        if (!(factionAt instanceof ClaimableFaction)) {
            result = true;
        } else if (factionAt instanceof Raidable && ((Raidable) factionAt).isRaidable()) {
            result = true;
        }

        if (player != null && factionAt instanceof PlayerFaction) {
            if (HCF.getPlugin().getFactionManager().getPlayerFaction(player) == factionAt) {
                result = true;
            }
        }

        if (result) {
            // Show this message last as the other messages look cleaner.
            if (!isInteraction && factionAt instanceof WarzoneFaction) {
                if (denyMessage != null && player != null) {
                    player.sendMessage(ChatColor.YELLOW + "You cannot build in the " + factionAt.getDisplayName(player) + ChatColor.YELLOW + ".");
                }

                return false;
            }
        } else if (denyMessage != null && player != null) {
            player.sendMessage(String.format(denyMessage, factionAt.getDisplayName(player)));
        }

        return result;
    }

    /**
     * Checks if a {@link Location} is eligible to build into another {@link Location}.
     *
     * @param from the from {@link Location} to test
     * @param to   the to {@link Location} to test
     * @return true if the to {@link Faction} is the same or is not claimable
     */
    public static boolean canBuildAt(Location from, Location to) {
        Faction toFactionAt = HCF.getPlugin().getFactionManager().getFactionAt(to);
        return !(toFactionAt instanceof Raidable && !((Raidable) toFactionAt).isRaidable() && toFactionAt != HCF.getPlugin().getFactionManager().getFactionAt(from));
    }
}
