package org.ipvp.hcf.command;

import com.doctordark.util.JavaUtils;
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
 * Command used to check the angle and yaw positions of {@link Player}s.
 */
public class AngleCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        Location location = ((Player) sender).getLocation();

        sender.sendMessage(ChatColor.GOLD + JavaUtils.format(location.getYaw()) + " yaw" + ChatColor.WHITE + ", " + ChatColor.GOLD + JavaUtils.format(location.getPitch()) + " pitch");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
