package org.ipvp.hcfextra.command;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.ipvp.hcfextra.HCFExtra;

import java.util.Collections;
import java.util.List;

public class HCFExtraCommand implements CommandExecutor, TabCompleter {

    private static final List<String> COMPLETIONS_FIRST = ImmutableList.of("reload");

    private final HCFExtra plugin;

    public HCFExtraCommand(HCFExtra plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.getConfiguration().reload();
                sender.sendMessage(ChatColor.RED + "Reloaded " + ChatColor.YELLOW + plugin.getDescription().getFullName() + ChatColor.RED + ".");
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <reload>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 1 ? COMPLETIONS_FIRST : Collections.emptyList();
    }
}
