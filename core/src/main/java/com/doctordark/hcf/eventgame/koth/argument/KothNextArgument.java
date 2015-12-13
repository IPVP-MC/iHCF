package com.doctordark.hcf.eventgame.koth.argument;

import com.doctordark.hcf.DateTimeFormats;
import com.doctordark.hcf.HCF;
import com.doctordark.util.command.CommandArgument;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An {@link CommandArgument} used to check the next running KingOfTheHill event.
 */
public class KothNextArgument extends CommandArgument {

    private final HCF plugin;

    public KothNextArgument(HCF plugin) {
        super("next", "View the next scheduled KOTH");
        this.plugin = plugin;
        this.permission = "hcf.command.koth.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        long millis = System.currentTimeMillis();
        sender.sendMessage(ChatColor.GOLD + "The server time is currently " + ChatColor.YELLOW + DateTimeFormats.DAY_MTH_HR_MIN_AMPM.format(millis) + ChatColor.GOLD + '.');

        Map<LocalDateTime, String> scheduleMap = plugin.getEventScheduler().getScheduleMap();

        if (scheduleMap.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There is not an event schedule for after now.");
            return true;
        }

        LocalDateTime now = LocalDateTime.now(plugin.getConfiguration().getServerTimeZoneID());

        for (Map.Entry<LocalDateTime, String> entry : scheduleMap.entrySet()) {
            // Only show the events that haven't been yet.
            LocalDateTime scheduleDateTime = entry.getKey();
            if (now.isAfter(scheduleDateTime)) continue;

            String monthName = scheduleDateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String weekName = scheduleDateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            sender.sendMessage(ChatColor.DARK_AQUA + WordUtils.capitalize(entry.getValue()) + ChatColor.GRAY + " is the next event: " +
                    ChatColor.AQUA + weekName + ' ' + scheduleDateTime.getDayOfMonth() + ' ' + monthName +
                    ChatColor.DARK_AQUA + " (" + DateTimeFormats.HR_MIN_AMPM.format(TimeUnit.HOURS.toMillis(scheduleDateTime.getHour()) +
                    TimeUnit.MINUTES.toMillis(scheduleDateTime.getMinute())) + ')');

            return true;
        }

        sender.sendMessage(ChatColor.RED + "There is not an event scheduled after now.");
        return true;
    }
}
