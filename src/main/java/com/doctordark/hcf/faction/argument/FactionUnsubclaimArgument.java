package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class FactionUnsubclaimArgument extends CommandArgument {

    private final HCF plugin;

    public FactionUnsubclaimArgument(HCF plugin) {
        super("unsubclaim", "Removes subclaims from your faction.");
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
            sender.sendMessage(ChatColor.RED + "You must be a faction leader to delete subclaims.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Please use /" + label + " <subclaim> <remove> for now.");

        //TODO: Bugfix, should remove SUBCLAIMS, not claims.
        /*Collection<Claim> factionClaims = playerFaction.getClaims();
        Location location = player.getLocation();

        // Find out what claims the player wants removed.
        Collection<Claim> removingClaims = null;
        Integer radius = null;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("all")) {
                removingClaims = factionClaims;
                radius = -1; // special case for this
            } else if ((radius = JavaUtils.tryParseInt(args[1])) != null) {
                if (radius <= 0) {
                    sender.sendMessage(ChatColor.RED + "Radius must be positive.");
                    return true;
                }

                removingClaims = null;
                sender.sendMessage(ChatColor.RED + "Removing subclaims by a radius is currently unimplemented.");
                return true;
            }
        }

        // Try and identify any here if none of the arguments above return.
        if (removingClaims == null) {
            Claim claimAt = plugin.getFactionManager().getClaimAt(location);
            removingClaims = claimAt != null ? Collections.singletonList(claimAt) : Collections.emptyList();
        }

        int removed = 0;
        for (Claim claim : removingClaims) {
            Iterator<Subclaim> iterator = claim.getSubclaims().iterator();
            while (iterator.hasNext()) {
                Subclaim subclaim = iterator.next();
                if (subclaim != null && subclaim.contains(location)) {
                    iterator.remove();
                    removed++;
                }
            }
        }

        if (removed == 0) {
            sender.sendMessage(ChatColor.RED + "Your faction does not own any subclaims" + (radius == null ? " here" : (radius == -1 ? "" : " within " + radius + " blocks of you")) + '.');
            return true;
        }

        if (playerFaction.removeClaims(factionClaims, player)) {
            playerFaction.broadcast(ChatColor.RED + ChatColor.BOLD.toString() + factionMember.getRole().getAstrix() +
                    sender.getName() + " has removed " + removed + " subclaim" + (removed > 1 ? "s" : "") + '.');
        }*/

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? COMPLETIONS : Collections.<String>emptyList();
    }

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
}
