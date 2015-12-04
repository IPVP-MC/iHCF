package com.doctordark.hcf.eventgame.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.command.CommandArgument;
import com.doctordark.util.cuboid.Cuboid;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@link CommandArgument} used for setting the area of an {@link EventFaction}.
 */
public class EventSetAreaArgument extends CommandArgument {

    private static final int MIN_EVENT_CLAIM_AREA = 8;

    private final HCF plugin;

    public EventSetAreaArgument(HCF plugin) {
        super("setarea", "Sets the area of an event");
        this.plugin = plugin;
        this.aliases = new String[]{"setclaim", "setclaimarea", "setland"};
        this.permission = "hcf.command.event.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <kothName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set event claim areas");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        WorldEditPlugin worldEditPlugin = plugin.getWorldEdit();

        if (worldEditPlugin == null) {
            sender.sendMessage(ChatColor.RED + "WorldEdit must be installed to set event claim areas.");
            return true;
        }

        Player player = (Player) sender;
        Selection selection = worldEditPlugin.getSelection(player);

        if (selection == null) {
            sender.sendMessage(ChatColor.RED + "You must make a WorldEdit selection to do this.");
            return true;
        }

        if (selection.getWidth() < MIN_EVENT_CLAIM_AREA || selection.getLength() < MIN_EVENT_CLAIM_AREA) {
            sender.sendMessage(ChatColor.RED + "Event claim areas must be at least " + MIN_EVENT_CLAIM_AREA + 'x' + MIN_EVENT_CLAIM_AREA + '.');
            return true;
        }

        Faction faction = plugin.getFactionManager().getFaction(args[1]);

        if (!(faction instanceof EventFaction)) {
            sender.sendMessage(ChatColor.RED + "There is not an event faction named '" + args[1] + "'.");
            return true;
        }

        ((EventFaction) faction).setClaim(new Cuboid(selection.getMinimumPoint(), selection.getMaximumPoint()), player);

        sender.sendMessage(ChatColor.YELLOW + "Updated the claim for event " + faction.getName() + ChatColor.YELLOW + '.');
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }

        return plugin.getFactionManager().getFactions().stream().filter(faction -> faction instanceof EventFaction).map(Faction::getName).collect(Collectors.toList());
    }
}
