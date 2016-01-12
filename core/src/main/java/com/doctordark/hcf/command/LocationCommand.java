package com.doctordark.hcf.command;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.Faction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Command used to check current the current {@link Faction} at
 * the position of a given {@link Player}s {@link Location}.
 */
public class LocationCommand implements CommandExecutor, TabCompleter {

    private final HCF plugin;

    public LocationCommand(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (args.length >= 1 && sender.hasPermission(command.getPermission() + ".others")) {
            target = Bukkit.getPlayer(args[0]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [playerName]");
            return true;
        }

        if (target == null || (sender instanceof Player && !((Player) sender).canSee(target))) {
            sender.sendMessage(ChatColor.GOLD + "Player '" + ChatColor.WHITE + args[0] + ChatColor.GOLD + "' not found.");
            return true;
        }

        Location location = target.getLocation();
        Faction factionAt = plugin.getFactionManager().getFactionAt(location);
        sender.sendMessage(ChatColor.YELLOW + target.getName() + " is in the territory of " + factionAt.getDisplayName(sender)
                + ChatColor.YELLOW + '(' + (factionAt.isSafezone() ? ChatColor.GREEN + "Non-Deathban" : ChatColor.RED + "Deathban") + ChatColor.YELLOW + ')');

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 1 && sender.hasPermission(command.getPermission() + ".others") ? null : Collections.emptyList();
    }
}
