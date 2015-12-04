package com.doctordark.hcf.eventgame.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * An {@link CommandArgument} used for cancelling the current running event.
 */
public class EventCancelArgument extends CommandArgument {

    private final HCF plugin;

    public EventCancelArgument(HCF plugin) {
        super("cancel", "Cancels a running event", new String[]{"stop", "end"});
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
        Faction eventFaction = eventTimer.getEventFaction();

        if (!eventTimer.clearCooldown()) {
            sender.sendMessage(ChatColor.RED + "There is not a running event.");
            return true;
        }

        Bukkit.broadcastMessage(sender.getName() + ChatColor.YELLOW + " has cancelled " + (eventFaction == null ? "the active event" : eventFaction.getName() + ChatColor.YELLOW) + ".");
        return true;
    }
}
