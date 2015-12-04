package com.doctordark.hcf.faction.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.economy.EconomyManager;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.event.FactionClaimChangeEvent;
import com.doctordark.hcf.faction.event.FactionClaimChangedEvent;
import com.doctordark.hcf.faction.event.cause.ClaimChangeCause;
import com.doctordark.util.BukkitUtils;
import com.doctordark.util.GenericUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a {@link Faction} that can claim land.
 */
public class ClaimableFaction extends Faction {

    protected final List<Claim> claims = new ArrayList<>();

    public ClaimableFaction(String name) {
        super(name);
    }

    public ClaimableFaction(Map<String, Object> map) {
        super(map);
        this.claims.addAll(GenericUtils.createList(map.get("claims"), Claim.class));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("claims", new ArrayList<>(claims));
        return map;
    }

    protected static final ImmutableMap<World.Environment, String> ENVIRONMENT_MAPPINGS = /*TODO:Maps.immutableEnumMap*/(ImmutableMap.of(
            World.Environment.NETHER, "Nether",
            World.Environment.NORMAL, "Overworld",
            World.Environment.THE_END, "The End"
    ));

    /**
     * Prints details about this {@link Faction} to a {@link CommandSender}.
     *
     * @param sender the sender to print to
     */
    @Override
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(' ' + getDisplayName(sender));
        for (Claim claim : claims) {
            Location location = claim.getCenter();
            sender.sendMessage(ChatColor.YELLOW + "  Location: " + ChatColor.RED +
                    '(' + ENVIRONMENT_MAPPINGS.get(location.getWorld().getEnvironment()) + ", " + location.getBlockX() + " | " + location.getBlockZ() + ')');
        }

        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    /**
     * Gets the {@link Claim}s owned by this {@link ClaimableFaction}.
     *
     * @return an immutable list of {@link Claim}s
     */
    public List<Claim> getClaims() {
        return ImmutableList.copyOf(this.claims);
    }

    /**
     * Gets the {@link Claim}s owned by this {@link ClaimableFaction}
     * in a specific world.
     *
     * @return an immutable list of {@link Claim}s
     */
    public List<Claim> getClaims(World world) {
        List<Claim> ret = new ArrayList<>();
        for (Claim claim : this.claims) {
            if (world.equals(claim.getWorld())) {
                ret.add(claim);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Adds a {@link Claim} for this {@link Faction}.
     *
     * @param claim  the {@link Claim} to add
     * @param sender the {@link CommandSender} adding claim
     * @return true if the {@link Claim} was successfully added
     */
    public boolean addClaim(Claim claim, @Nullable CommandSender sender) {
        return addClaims(Collections.singleton(claim), sender);
    }

    /**
     * Adds a collection of {@link Claim}s to this {@link ClaimableFaction}.
     *
     * @param adding the {@link Claim}s to add
     * @param sender the {@link CommandSender} adding the {@link Claim}s
     * @return true if the {@link Claim}s were successfully added
     */
    public boolean addClaims(Collection<Claim> adding, @Nullable CommandSender sender) {
        if (sender == null) sender = Bukkit.getConsoleSender();

        FactionClaimChangeEvent event = new FactionClaimChangeEvent(sender, ClaimChangeCause.CLAIM, adding, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !claims.addAll(adding)) {
            return false;
        }

        Bukkit.getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.CLAIM, adding));
        return true;
    }

    /**
     * Removes a {@link Claim} for this {@link Faction}.
     *
     * @param claim  the {@link Claim} to remove
     * @param sender the {@link CommandSender} removing {@link Claim}
     * @return true if the {@link Claim} was successfully removed
     */
    public boolean removeClaim(Claim claim, @Nullable CommandSender sender) {
        return removeClaims(Collections.singleton(claim), sender);
    }

    /**
     * Removes a collection of {@link Claim}s for this {@link Faction}.
     *
     * @param toRemove the {@link Claim}s to remove
     * @param sender   the {@link CommandSender} removing {@link Claim}s
     * @return true if the {@link Claim}s were successfully removed
     */
    public boolean removeClaims(Collection<Claim> toRemove, @Nullable CommandSender sender) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }

        int expected = this.claims.size() - toRemove.size();

        FactionClaimChangeEvent event = new FactionClaimChangeEvent(sender, ClaimChangeCause.UNCLAIM, new ArrayList<>(toRemove), this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !this.claims.removeAll(toRemove)) { // we clone the collection so we can show what we removed to the event.
            return false;
        }

        if (expected != this.claims.size()) {
            return false;
        }

        if (this instanceof PlayerFaction) {
            PlayerFaction playerFaction = (PlayerFaction) this;
            Location home = playerFaction.getHome();
            HCF plugin = HCF.getPlugin();

            int refund = 0;
            for (Claim claim : toRemove) {
                refund += plugin.getClaimHandler().calculatePrice(claim, expected, true);
                if (expected > 0) expected--;

                if (home != null && claim.contains(home)) {
                    playerFaction.setHome(null);
                    playerFaction.broadcast(ChatColor.RED.toString() + ChatColor.BOLD + "Your factions' home was unset as its residing claim was removed.");
                    break;
                }
            }

            plugin.getEconomyManager().addBalance(playerFaction.getLeader().getUniqueId(), refund);
            playerFaction.broadcast(ChatColor.YELLOW + "Faction leader was refunded " + ChatColor.GREEN + EconomyManager.ECONOMY_SYMBOL + refund + ChatColor.YELLOW + " due to a land unclaim.");
        }

        Bukkit.getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.UNCLAIM, toRemove));
        return true;
    }
}
