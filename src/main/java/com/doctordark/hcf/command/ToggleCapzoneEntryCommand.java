package com.doctordark.hcf.command;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Command used to toggle messages shown when entering or leaving
 */
public class ToggleCapzoneEntryCommand implements CommandExecutor, TabExecutor {

    private final HCF plugin;

    public ToggleCapzoneEntryCommand(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        FactionUser factionUser = plugin.getUserManager().getUser(((Player) sender).getUniqueId());
        boolean newStatus = !factionUser.isCapzoneEntryAlerts();
        factionUser.setCapzoneEntryAlerts(newStatus);

        sender.sendMessage(ChatColor.AQUA + "You will now " + (newStatus ? ChatColor.GREEN.toString() : ChatColor.RED + "un") + "able" +
                ChatColor.AQUA + " to see capture zone entry messages.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
