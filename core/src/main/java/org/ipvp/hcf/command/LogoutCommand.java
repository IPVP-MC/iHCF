package org.ipvp.hcf.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.timer.type.LogoutTimer;

import java.util.Collections;
import java.util.List;

public class LogoutCommand implements CommandExecutor, TabCompleter {

    private final HCF plugin;

    public LogoutCommand(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        Player player = (Player) sender;
        LogoutTimer logoutTimer = plugin.getTimerManager().getLogoutTimer();

        if (!logoutTimer.setCooldown(player, player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Your " + logoutTimer.getDisplayName() + ChatColor.RED + " timer is already active.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Your " + logoutTimer.getDisplayName() + ChatColor.RED + " timer has started.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
