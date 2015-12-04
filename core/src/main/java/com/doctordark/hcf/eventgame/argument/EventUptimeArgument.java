package com.doctordark.hcf.eventgame.argument;

import com.doctordark.hcf.DateTimeFormats;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.util.command.CommandArgument;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * A {@link CommandArgument} argument used for checking the uptime of current event.
 */
public class EventUptimeArgument extends CommandArgument {

    private final HCF plugin;

    public EventUptimeArgument(HCF plugin) {
        super("uptime", "Check the uptime of an event");
        this.plugin = plugin;
        this.permission = "hcf.command.event.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        EventTimer eventTimer = plugin.getTimerManager().getEventTimer();

        if (eventTimer.getRemaining() <= 0L) {
            sender.sendMessage(ChatColor.RED + "There is not a running event.");
            return true;
        }

        EventFaction eventFaction = eventTimer.getEventFaction();
        sender.sendMessage(ChatColor.YELLOW + "Up-time of " + eventTimer.getName() + " timer" +
                (eventFaction == null ? "" : ": " + ChatColor.BLUE + '(' + eventFaction.getDisplayName(sender) + ChatColor.BLUE + ')') +
                ChatColor.YELLOW + " is " + ChatColor.GRAY + DurationFormatUtils.formatDurationWords(eventTimer.getUptime(), true, true) + ChatColor.YELLOW + ", started at " +
                ChatColor.GOLD + DateTimeFormats.HR_MIN_AMPM_TIMEZONE.format(eventTimer.getStartStamp()) + ChatColor.YELLOW + '.');

        return true;
    }
}
