package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionShowArgument extends CommandArgument {

    private final HCF plugin;

    public FactionShowArgument(HCF plugin) {
        super("show", "Get details about a faction.", new String[]{"i", "info", "who"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " [playerName|factionName]";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Faction playerFaction = null;
        Faction namedFaction;

        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
                return true;
            }

            namedFaction = plugin.getFactionManager().getPlayerFaction((Player) sender);

            if (namedFaction == null) {
                sender.sendMessage(ChatColor.RED + "You are not in a faction.");
                return true;
            }
        } else {
            namedFaction = plugin.getFactionManager().getFaction(args[1]);
            playerFaction = plugin.getFactionManager().getContainingPlayerFaction(args[1]);

            if (namedFaction == null && playerFaction == null) {
                sender.sendMessage(ChatColor.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
                return true;
            }
        }

        if (namedFaction != null) {
            namedFaction.printDetails(sender);
        }

        if (playerFaction != null && namedFaction != playerFaction) {
            playerFaction.printDetails(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args[1].isEmpty()) {
            return null;
        }

        Player player = (Player) sender;
        List<String> results = new ArrayList<>(plugin.getFactionManager().getFactionNameMap().keySet());
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player.canSee(target) && !results.contains(target.getName())) {
                results.add(target.getName());
            }
        }

        return results;
    }
}
