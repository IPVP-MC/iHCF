package org.ipvp.hcf.faction.argument.subclaim;

import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.claim.Claim;
import org.ipvp.hcf.faction.claim.Subclaim;
import org.ipvp.hcf.faction.struct.Role;
import org.ipvp.hcf.faction.type.PlayerFaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FactionSubclaimDeleteArgument extends CommandArgument {

    private final HCF plugin;

    public FactionSubclaimDeleteArgument(HCF plugin) {
        super("delete", "Remove a subclaim", new String[]{"del", "remove"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + " subclaim " + getName() + " <subclaimName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        if (args.length < 3) {
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

        for (Claim claim : playerFaction.getClaims()) {
            for (Iterator<Subclaim> iterator = claim.getSubclaims().iterator(); iterator.hasNext(); ) {
                Subclaim subclaim = iterator.next();
                if (subclaim.getName().equalsIgnoreCase(args[2])) {
                    iterator.remove();
                    sender.sendMessage(ChatColor.AQUA + "Removed subclaim named " + subclaim.getName() + '.');
                    return true;
                }
            }
        }

        sender.sendMessage(ChatColor.RED + "Your faction does not have a subclaim named " + args[2] + '.');
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null || playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        for (Claim claim : playerFaction.getClaims()) {
            results.addAll(claim.getSubclaims().stream().map(Subclaim::getName).collect(Collectors.toList()));
        }

        return results;
    }
}
