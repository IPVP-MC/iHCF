package com.doctordark.hcf.command;

import com.doctordark.hcf.ConfigurationService;
import org.apache.commons.lang3.time.FastDateFormat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Command used to check the current time for the server.
 */
public class ServerTimeCommand implements CommandExecutor, TabCompleter {

    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("E MMM dd h:mm:ssa z yyyy", ConfigurationService.SERVER_TIME_ZONE, Locale.ENGLISH);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "The server time is " + ChatColor.LIGHT_PURPLE +
                FORMAT.format(LocalDateTime.now(ConfigurationService.SERVER_TIME_ZONE_ID)) + ChatColor.GREEN + '.');

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
