package org.ipvp.hcf.eventgame.koth.argument;

import com.doctordark.util.BukkitUtils;
import com.doctordark.util.command.CommandArgument;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ipvp.hcf.DateTimeFormats;
import org.ipvp.hcf.HCF;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * An {@link CommandArgument} used to view schedules for KingOfTheHill games.
 */
public class KothScheduleArgument extends CommandArgument {

    private static final String TIME_UNTIL_PATTERN = "d'd' H'h' mm'm'";
    private static final DateTimeFormatter HHMMA = DateTimeFormatter.ofPattern("h:mma");

    // The time of event schedules, the String as the faction name.
    private final HCF plugin;

    public KothScheduleArgument(HCF plugin) {
        super("schedule", "View the schedule for KOTH arenas");
        this.plugin = plugin;
        this.aliases = new String[]{"info", "i", "time"};
        this.permission = "hcf.command.koth.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LocalDateTime now = LocalDateTime.now(DateTimeFormats.SERVER_ZONE_ID);
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

                String monthName = scheduleDateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                String weekName = scheduleDateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                ChatColor colour = dayDifference == 0 ? ChatColor.GREEN : ChatColor.AQUA;
                shownEvents.add("  " + colour + WordUtils.capitalizeFully(entry.getValue()) + ": " + ChatColor.YELLOW +
                        weekName + ' ' + scheduleDateTime.getDayOfMonth() + ' ' + monthName + ChatColor.RED + " (" + HHMMA.format(scheduleDateTime) + ')' +
                        ChatColor.GRAY + " - " + ChatColor.GOLD +
                        DurationFormatUtils.formatDuration(now.until(scheduleDateTime, ChronoUnit.MILLIS), TIME_UNTIL_PATTERN));
            }
        }

        if (shownEvents.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There are no event schedules defined.");
            return true;
        }

        String monthName = WordUtils.capitalizeFully(now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        String weekName = WordUtils.capitalizeFully(now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        sender.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ChatColor.GRAY + "Server time is currently " + ChatColor.WHITE +
                weekName + ' ' + now.getDayOfMonth() + ' ' + monthName + ' ' + HHMMA.format(now) + ChatColor.GRAY + '.');
        sender.sendMessage(shownEvents.toArray(new String[shownEvents.size()]));
        sender.sendMessage(ChatColor.GRAY + "For more info about King of the Hill, use " + ChatColor.WHITE + '/' + label + " help" + ChatColor.GRAY + '.');
        sender.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);

        return true;
    }
}
