package com.doctordark.hcf.faction.claim;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.economy.EconomyManager;
import com.doctordark.hcf.faction.FactionManager;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.ClaimableFaction;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.hcf.faction.type.RoadFaction;
import com.doctordark.hcf.faction.type.WildernessFaction;
import com.doctordark.hcf.visualise.VisualType;
import com.doctordark.util.ItemBuilder;
import com.doctordark.util.cuboid.Cuboid;
import com.doctordark.util.cuboid.CuboidDirection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClaimHandler {

    public static final int MIN_CLAIM_HEIGHT = 0;
    public static final int MAX_CLAIM_HEIGHT = 256;

    public static final long PILLAR_BUFFER_DELAY_MILLIS = 200L;

    public static final ItemStack SUBCLAIM_WAND = new ItemBuilder(Material.GOLD_SPADE, 1).displayName(ChatColor.GOLD + "Subclaim Wand").lore(
            ChatColor.AQUA + "Left or Right Click " + ChatColor.GREEN + "a Block" + ChatColor.AQUA + " to:",
            ChatColor.GRAY + "Set the first and second position of ",
            ChatColor.GRAY + "your Subclaim selection.",
            "",
            ChatColor.AQUA + "Right Click " + ChatColor.GREEN + "the Air" + ChatColor.AQUA + " to:",
            ChatColor.GRAY + "Clear your current Subclaim selection.",
            "",
            ChatColor.AQUA + "Use " + ChatColor.YELLOW + "/faction subclaim create <name>" + ChatColor.AQUA + " to:",
            ChatColor.GRAY + "Acquire your selected Subclaim.").build();

    public static final ItemStack CLAIM_WAND = new ItemBuilder(Material.DIAMOND_HOE).displayName(ChatColor.RED + "Claim Wand").lore(
            ChatColor.AQUA + "Left or Right Click " + ChatColor.GREEN + "a Block" + ChatColor.AQUA + " to:",
            ChatColor.GRAY + "Set the first and second position of ",
            ChatColor.GRAY + "your Claim selection.",
            "",
            ChatColor.AQUA + "Right Click " + ChatColor.GREEN + "the Air" + ChatColor.AQUA + " to:",
            ChatColor.GRAY + "Clear your current Claim selection.",
            "",
            ChatColor.YELLOW + "Shift " + ChatColor.AQUA + "Left Click " + ChatColor.GREEN + "the Air or a Block" + ChatColor.AQUA + " to:",
            ChatColor.GRAY + "Purchase your current Claim selection.").build();

    private static final int NEXT_PRICE_MULTIPLIER_AREA = 250;   // the area a claim cuboid needs until the price multiplier is increased
    private static final int NEXT_PRICE_MULTIPLIER_CLAIM = 500;  // the amount each claim a faction has will add onto the final price

    public static final int MIN_SUBCLAIM_RADIUS = 2;
    public static final int MIN_CLAIM_RADIUS = 5;
    public static final int MAX_CHUNKS_PER_LIMIT = 16;
    public static final int CLAIM_BUFFER_RADIUS = 4;

    public final Map<UUID, ClaimSelection> claimSelectionMap;
    private final HCF plugin;

    public ClaimHandler(HCF plugin) {
        this.plugin = plugin;
        this.claimSelectionMap = new HashMap<>();
    }

    //TODO: Better configurability
    private static final double CLAIM_SELL_MULTIPLIER = 0.8;
    private static final double CLAIM_PRICE_PER_BLOCK = 0.25;

    /**
     * Gets the price of this {@link Claim} for a given {@link Faction}.
     *
     * @param claim         the {@link Cuboid} to calculate
     * @param currentClaims the current amount of claims the object being looked up has
     * @param selling       if the {@link Faction} is selling the claim
     * @return the price of the {@link Claim}
     */
    public int calculatePrice(Cuboid claim, int currentClaims, boolean selling) {
        if (currentClaims == -1 || !claim.hasBothPositionsSet()) {
            return 0;
        }

        int multiplier = 1;
        int remaining = claim.getArea();
        double price = 0;
        while (remaining > 0) {
            if (--remaining % NEXT_PRICE_MULTIPLIER_AREA == 0) {
                multiplier++;
            }

            price += (CLAIM_PRICE_PER_BLOCK * multiplier);
        }

        if (currentClaims != 0) {
            currentClaims = Math.max(currentClaims + (selling ? -1 : 0), 0);
            price += (currentClaims * NEXT_PRICE_MULTIPLIER_CLAIM);
        }

        if (selling) {
            price *= CLAIM_SELL_MULTIPLIER; // if selling the claim, make the price cheaper (currently 80%).
        }

        return (int) price;
    }

    public boolean clearClaimSelection(Player player) {
        ClaimSelection claimSelection = plugin.getClaimHandler().claimSelectionMap.remove(player.getUniqueId());
        if (claimSelection != null) {
            plugin.getVisualiseHandler().clearVisualBlocks(player, VisualType.CREATE_CLAIM_SELECTION, null);
            return true;
        }

        return false;
    }

    /**
     * Checks if a {@link Player} is eligible to {@link Subclaim} at a
     * given {@link Location}.
     *
     * @param player   the {@link Player} to check for
     * @param location the {@link Location} to check at
     * @return true if the {@link Player} can Subclaim at the {@link Location}
     */
    public boolean canSubclaimHere(Player player, Location location) {
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to subclaim land.");
            return false;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(ChatColor.RED + "You must be an officer to claim land.");
            return false;
        }

        if (plugin.getFactionManager().getFactionAt(location) != playerFaction) {
            player.sendMessage(ChatColor.RED + "This location is not part of your factions' territory.");
            return false;
        }

        return true;
    }

    /**
     * Tries to {@link Subclaim} land for a {@link Player}s {@link PlayerFaction}'.
     *
     * @param player   the {@link Player} that is attempting to create the {@link Subclaim}
     * @param subclaim the {@link Subclaim} to be created
     * @return true if {@link Player} could create the {@link Subclaim}
     */
    public boolean tryCreatingSubclaim(Player player, Subclaim subclaim) {
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to subclaim land.");
            return false;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(ChatColor.RED + "You must be an officer to create subclaims.");
            return true;
        }

        World world = subclaim.getWorld();
        int minimumX = subclaim.getMinimumX();
        int maximumX = subclaim.getMaximumX();
        int minimumZ = subclaim.getMinimumZ();
        int maximumZ = subclaim.getMaximumZ();

        // Some counters used to determine how
        // many faction claims are within the cuboid.
        Claim foundClaim = null;
        for (int x = minimumX; x < maximumX; x++) {
            for (int z = minimumZ; z < maximumZ; z++) {
                Faction factionAt; // lazy load
                Claim claimAt = plugin.getFactionManager().getClaimAt(world, x, z);
                if (claimAt == null || playerFaction == (factionAt = plugin.getFactionManager().getFactionAt(world, x, z)) && !(factionAt instanceof PlayerFaction)) {
                    player.sendMessage(ChatColor.RED + "This subclaim selection contains a location outside of your faction.");
                    return false;
                }

                if (playerFaction.getClaims().contains(claimAt)) {
                    for (Subclaim claimAtSubclaims : claimAt.getSubclaims()) {
                        if (claimAtSubclaims.contains(world, x, z)) {
                            player.sendMessage(ChatColor.RED + "Subclaims cannot overlap each other.");
                            return false;
                        }
                    }
                }

                if (foundClaim == null) {
                    foundClaim = claimAt;
                } else if (claimAt != foundClaim) {
                    player.sendMessage(ChatColor.RED + "This subclaim selection is inside more than one of your faction claims.");
                    return false;
                }
            }
        }

        if (foundClaim == null) {
            player.sendMessage(ChatColor.RED + "This subclaim selection is not inside your faction territory.");
            return false;
        }

        foundClaim.getSubclaims().add(subclaim);
        subclaim.getAccessibleMembers().add(player.getUniqueId());

        player.sendMessage(ChatColor.GOLD + "You have created a subclaim named " + ChatColor.AQUA + subclaim.getName() + ChatColor.GOLD + '.');
        return true;
    }

    /**
     * Checks if a {@link Player} is eligible to {@link Claim} at a  given {@link Location}.
     *
     * @param player   the {@link Player} to check for
     * @param location the {@link Location} to check at
     * @return true if the {@link Player} can Claim at the {@link Location}
     */
    public boolean canClaimHere(Player player, Location location) {
        World world = location.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(ChatColor.RED + "You can only claim land in the Overworld.");
            return false;
        }

        if (!(plugin.getFactionManager().getFactionAt(location) instanceof WildernessFaction)) {
            player.sendMessage(ChatColor.RED + "You can only claim land in the " + plugin.getConfiguration().getRelationColourWilderness() + "Wilderness" + ChatColor.RED + ". " +
                    "Make sure you are past " + plugin.getConfiguration().getWarzoneRadiusOverworld() + " blocks from spawn..");

            return false;
        }

        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to claim land.");
            return false;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(ChatColor.RED + "You must be an officer to claim land.");
            return false;
        }

        if (playerFaction.getClaims().size() >= plugin.getConfiguration().getFactionMaxClaims()) {
            player.sendMessage(ChatColor.RED + "Your faction has maximum claims possible, which is " + plugin.getConfiguration().getFactionMaxClaims() + ".");
            return false;
        }

        int locX = location.getBlockX();
        int locZ = location.getBlockZ();

        final FactionManager factionManager = plugin.getFactionManager();
        boolean flag = HCF.getPlugin().getConfiguration().isAllowClaimsBesidesRoads();
        for (int x = locX - CLAIM_BUFFER_RADIUS; x < locX + CLAIM_BUFFER_RADIUS; x++) {
            for (int z = locZ - CLAIM_BUFFER_RADIUS; z < locZ + CLAIM_BUFFER_RADIUS; z++) {
                Faction factionAtNew = factionManager.getFactionAt(world, x, z);
                if (!flag && factionAtNew instanceof ClaimableFaction && playerFaction != factionAtNew && !(factionAtNew instanceof RoadFaction)) {
                    player.sendMessage(ChatColor.RED + "This position contains enemy claims within a " + CLAIM_BUFFER_RADIUS + " block buffer radius.");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Tries to purchase a {@link Claim} for a {@link Player}s {@link PlayerFaction}'.
     *
     * @param player the {@link Player} that is attempting to create the {@link Subclaim}
     * @param claim  the {@link Claim} to be created
     * @return true if {@link Player} could create the {@link Subclaim}
     */
    public boolean tryPurchasing(Player player, Claim claim) {
        Objects.requireNonNull(claim, "Claim is null");
        World world = claim.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(ChatColor.RED + "You can only claim land in the Overworld.");
            return false;
        }

        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to claim land.");
            return false;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(ChatColor.RED + "You must be an officer to claim land.");
            return false;
        }

        if (playerFaction.getClaims().size() >= plugin.getConfiguration().getFactionMaxClaims()) {
            player.sendMessage(ChatColor.RED + "Your faction has maximum claims possible, which is " + plugin.getConfiguration().getFactionMaxClaims() + ".");
            return false;
        }

        int factionBalance = playerFaction.getBalance();
        int claimPrice = calculatePrice(claim, playerFaction.getClaims().size(), false);

        if (claimPrice > factionBalance) {
            player.sendMessage(ChatColor.RED + "Your faction bank only has " + EconomyManager.ECONOMY_SYMBOL + factionBalance + ", the price of this claim is " +
                    EconomyManager.ECONOMY_SYMBOL + claimPrice + '.');

            return false;
        }

        if (claim.getChunks().size() > MAX_CHUNKS_PER_LIMIT) {
            player.sendMessage(ChatColor.RED + "Claims cannot exceed " + MAX_CHUNKS_PER_LIMIT + " chunks.");
            return false;
        }

        // Is not enough blocks wide.
        if (claim.getWidth() < MIN_CLAIM_RADIUS || claim.getLength() < MIN_CLAIM_RADIUS) {
            player.sendMessage(ChatColor.RED + "Claims must be at least " + MIN_CLAIM_RADIUS + 'x' + MIN_CLAIM_RADIUS + " blocks.");
            return false;
        }

        int minimumX = claim.getMinimumX();
        int maximumX = claim.getMaximumX();
        int minimumZ = claim.getMinimumZ();
        int maximumZ = claim.getMaximumZ();

        final FactionManager factionManager = plugin.getFactionManager();
        for (int x = minimumX; x < maximumX; x++) {
            for (int z = minimumZ; z < maximumZ; z++) {
                Faction factionAt = factionManager.getFactionAt(world, x, z);
                if (factionAt != null && !(factionAt instanceof WildernessFaction)) {
                    player.sendMessage(ChatColor.RED + "This claim contains a location not within the " + ChatColor.GRAY + "Wilderness" + ChatColor.RED + '.');
                    return false;
                }
            }
        }

        boolean flag = HCF.getPlugin().getConfiguration().isAllowClaimsBesidesRoads();
        for (int x = minimumX - CLAIM_BUFFER_RADIUS; x < maximumX + CLAIM_BUFFER_RADIUS; x++) {
            for (int z = minimumZ - CLAIM_BUFFER_RADIUS; z < maximumZ + CLAIM_BUFFER_RADIUS; z++) {
                Faction factionAtNew = factionManager.getFactionAt(world, x, z);
                if (!flag && factionAtNew instanceof ClaimableFaction && playerFaction != factionAtNew && !(factionAtNew instanceof RoadFaction)) {
                    player.sendMessage(ChatColor.RED + "This claim contains enemy claims within a " + CLAIM_BUFFER_RADIUS + " block buffer radius.");
                    return false;
                }
            }
        }

        Location minimum = claim.getMinimumPoint();
        Location maximum = claim.getMaximumPoint();

        Collection<Claim> otherClaims = playerFaction.getClaims();
        boolean conjoined = otherClaims.isEmpty();
        if (!conjoined) {
            for (Claim otherClaim : otherClaims) {
                Cuboid outset = otherClaim.clone().outset(CuboidDirection.HORIZONTAL, 1);
                if (outset.contains(minimum) || outset.contains(maximum)) {
                    conjoined = true;
                    break;
                }
            }

            if (!conjoined) {
                player.sendMessage(ChatColor.RED + "All claims in your faction must be conjoined.");
                return false;
            }
        }

        // Fit the region.
        claim.setY1(ClaimHandler.MIN_CLAIM_HEIGHT);
        claim.setY2(ClaimHandler.MAX_CLAIM_HEIGHT);

        if (!playerFaction.addClaim(claim, player)) return false;
        Location center = claim.getCenter();
        player.sendMessage(ChatColor.AQUA + "Claim has been purchased for " + ChatColor.GREEN + EconomyManager.ECONOMY_SYMBOL + claimPrice + ChatColor.AQUA + '.');
        playerFaction.setBalance(factionBalance - claimPrice);
        playerFaction.broadcast(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " claimed land for your faction at " +
                ChatColor.GOLD + '(' + center.getBlockX() + ", " + center.getBlockZ() + ')' + ChatColor.GREEN + '.', player.getUniqueId());

        return true;
    }
}
