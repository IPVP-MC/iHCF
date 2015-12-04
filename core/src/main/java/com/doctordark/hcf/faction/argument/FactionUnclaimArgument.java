package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FactionUnclaimArgument extends CommandArgument {

    private final HCF plugin;

    public FactionUnclaimArgument(HCF plugin) {
        super("unclaim", "Unclaims land from your faction.");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " [all]";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can un-claim land from a faction.");
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        FactionMember factionMember = playerFaction.getMember(player);

        if (factionMember.getRole() != Role.LEADER) {
            sender.sendMessage(ChatColor.RED + "You must be a faction leader to unclaim land.");
            return true;
        }

        Collection<Claim> factionClaims = playerFaction.getClaims();

        if (factionClaims.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Your faction does not own any claims.");
            return true;
        }

        // Find out what claims the player wants removed.
        Collection<Claim> removingClaims;
        if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
            removingClaims = new ArrayList<>(factionClaims);
        } else {
            Location location = player.getLocation();
            Claim claimAt = plugin.getFactionManager().getClaimAt(location);
            if (claimAt == null || !factionClaims.contains(claimAt)) {
                sender.sendMessage(ChatColor.RED + "Your faction does not own a claim here.");
                return true;
            }

            removingClaims = Collections.singleton(claimAt);
        }

        if (!playerFaction.removeClaims(removingClaims, player)) {
            sender.sendMessage(ChatColor.RED + "Error when removing claims, please contact an Administrator.");
            return true;
        }

        int removingAmount = removingClaims.size();
        playerFaction.broadcast(ChatColor.RED + ChatColor.BOLD.toString() + factionMember.getRole().getAstrix() +
                sender.getName() + " has removed " + removingAmount + " claim" + (removingAmount > 1 ? "s" : "") + '.');

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? COMPLETIONS : Collections.<String>emptyList();
    }

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
}
