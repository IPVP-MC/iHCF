package com.doctordark.hcf.faction.argument.subclaim;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.claim.Subclaim;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Faction subclaim argument used to remove a members access to a {@link Subclaim}.
 */
public class FactionSubclaimDelMemberArgument extends CommandArgument {

    private final HCF plugin;

    public FactionSubclaimDelMemberArgument(HCF plugin) {
        super("delmember", "Removes a member from a subclaim", new String[]{"addplayer", "deleteplayer", "removeplayer", "delplayer", "revoke"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + " subclaim " + getName() + " <subclaimName> <memberName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            sender.sendMessage(ChatColor.RED + "You must be a faction officer to edit subclaims.");
            return true;
        }

        Subclaim subclaim = null;
        for (Claim claim : playerFaction.getClaims()) {
            if ((subclaim = claim.getSubclaim(args[2])) != null) {
                break;
            }
        }

        if (subclaim == null) {
            sender.sendMessage(ChatColor.RED + "Your faction does not have a subclaim named " + args[2] + '.');
            return true;
        }

        FactionMember factionTarget = playerFaction.getMember(args[3]);

        if (factionTarget == null) {
            sender.sendMessage(ChatColor.RED + "Your faction does not have a member named " + args[3] + '.');
            return true;
        }

        if (factionTarget.getRole() != Role.MEMBER) {
            sender.sendMessage(ChatColor.RED + "Faction officers already bypass subclaim protection.");
            return true;
        }

        if (!subclaim.getAccessibleMembers().remove(factionTarget.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + factionTarget.getName() + " will continue to not have access to subclaim " + subclaim.getName() + '.');
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Removed member " + factionTarget.getName() + " from subclaim " + subclaim.getName() + '.');
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            return Collections.emptyList();
        }

        switch (args.length) {
            case 3:
                if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
                    return Collections.emptyList();
                }

                List<String> results = new ArrayList<>();
                for (Claim claim : playerFaction.getClaims()) {
                    results.addAll(claim.getSubclaims().stream().map(Subclaim::getName).collect(Collectors.toList()));
                }

                return results;
            case 4:
                Subclaim subclaim = null;
                for (Claim claim : playerFaction.getClaims()) {
                    Subclaim next = claim.getSubclaim(args[2]);
                    if (next != null) {
                        subclaim = next;
                        break;
                    }
                }

                if (subclaim == null) {
                    return Collections.emptyList();
                }

                return playerFaction.getMembers().values().stream().filter(factionMember -> factionMember.getRole() == Role.MEMBER).map(FactionMember::getName).collect(Collectors.toList());
            default:
                return null;
        }
    }
}
