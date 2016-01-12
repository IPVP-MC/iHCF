package com.doctordark.hcf.eventgame.koth.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.util.BukkitUtils;
import com.doctordark.util.command.CommandArgument;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;

/**
 * An {@link CommandArgument} used to view schedules for KingOfTheHill games.
 */
public class KothScheduleArgument extends CommandArgument {

    private static final String TIME_UNTIL_PATTERN = "d'd' H'h' mm'm'";

    private final FastDateFormat headingTimeFormat;
    private final FastDateFormat eachKothTimeFormat;

    // The time of event schedules, the String as the faction name.
    private final HCF plugin;

    public KothScheduleArgument(HCF plugin) {
        super("schedule", "View the schedule for KOTH arenas");

        this.plugin = plugin;
        this.aliases = new String[]{"info", "i", "time"};
        this.permission = "hcf.command.koth.argument." + getName();

        TimeZone timeZone = plugin.getConfiguration().getServerTimeZone();
        this.headingTimeFormat = FastDateFormat.getInstance("EEE FF h:mma (z)", timeZone, Locale.ENGLISH);
        this.eachKothTimeFormat = FastDateFormat.getInstance("EEE dd '" + Matcher.quoteReplacement("&b") + "'(h:mma)", timeZone, Locale.ENGLISH);
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LocalDateTime now = LocalDateTime.now(plugin.getConfiguration().getServerTimeZoneID());
        int currentDay = now.getDayOfYear();

        Map<LocalDateTime, String> scheduleMap = plugin.getEventScheduler().getScheduleMap();
        List<String> shownEvents = new ArrayList<>();
        for (Map.Entry<LocalDateTime, String> entry : scheduleMap.entrySet()) {
            LocalDateTime scheduleDateTime = entry.getKey();
            if (scheduleDateTime.isAfter(now)) { // only show the events that haven't been scheduled yet.
                int dayDifference = scheduleDateTime.getDayOfYear() - currentDay;
                if (dayDifference > 1) {
                    continue; // only show events today or tomorrow.
                }

                ChatColor colour = dayDifference == 0 ? ChatColor.GREEN : ChatColor.AQUA;
                long remainingMillis = now.until(scheduleDateTime, ChronoUnit.MILLIS);
                shownEvents.add("  " + colour + WordUtils.capitalize(entry.getValue()) + ": " + ChatColor.YELLOW +
                        ChatColor.translateAlternateColorCodes('&', eachKothTimeFormat.format(remainingMillis)) +
                        ChatColor.GRAY + " - " + ChatColor.GOLD + DurationFormatUtils.formatDuration(remainingMillis, TIME_UNTIL_PATTERN));
            }
        }

        if (shownEvents.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There are no event schedules defined.");
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ChatColor.GRAY + "Server time is currently " + ChatColor.WHITE + headingTimeFormat.format(System.currentTimeMillis()) + ChatColor.GRAY + '.');
        sender.sendMessage(shownEvents.toArray(new String[shownEvents.size()]));
        sender.sendMessage(ChatColor.GRAY + "For more info about King of the Hill, use " + ChatColor.WHITE + '/' + label + " help" + ChatColor.GRAY + '.');
        sender.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);

        return true;
    }
}
