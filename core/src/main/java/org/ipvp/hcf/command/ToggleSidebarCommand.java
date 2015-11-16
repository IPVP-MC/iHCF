package org.ipvp.hcf.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.scoreboard.PlayerBoard;

import java.util.Collections;
import java.util.List;

/**
 * Command used to toggle the sidebar for a {@link Player}.
 */
public class ToggleSidebarCommand implements CommandExecutor, TabExecutor {

    private final HCF plugin;

    public ToggleSidebarCommand(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        PlayerBoard playerBoard = plugin.getScoreboardHandler().getPlayerBoard(((Player) sender).getUniqueId());
        boolean newVisibile = !playerBoard.isSidebarVisible();
        playerBoard.setSidebarVisible(newVisibile);

        sender.sendMessage(ChatColor.YELLOW + "Scoreboard sidebar is " + (newVisibile ? ChatColor.GREEN + "now" : ChatColor.RED + "no longer") + ChatColor.YELLOW + " visible.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}