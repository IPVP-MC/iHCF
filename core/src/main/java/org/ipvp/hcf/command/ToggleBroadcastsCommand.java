package org.ipvp.hcf.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.ipvp.hcf.ConfigurationService;

import java.util.Collections;
import java.util.List;

public class ToggleBroadcastsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean newStatus = !ConfigurationService.DIAMOND_ORE_ALERTS;
        ConfigurationService.DIAMOND_ORE_ALERTS = newStatus;
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + sender.getName() + " has " + (newStatus ? "enabled" : "disabled") + " found diamond ore notifications.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command commanda, String label, String[] args) {
        return Collections.emptyList();
    }
}
