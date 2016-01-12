package com.doctordark.hcf.eventgame.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.faction.CapturableFaction;
import com.doctordark.hcf.eventgame.faction.ConquestFaction;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import com.doctordark.hcf.eventgame.tracker.ConquestTracker;
import com.doctordark.hcf.eventgame.tracker.KothTracker;
import com.doctordark.hcf.faction.FactionManager;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.command.CommandArgument;
import com.google.common.base.Joiner;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@link CommandArgument} used for setting the {@link CaptureZone}s of an {@link EventFaction}.
 */
public class EventSetCapzoneArgument extends CommandArgument {

    private final HCF plugin;

    public EventSetCapzoneArgument(HCF plugin) {
        super("setcapzone", "Sets the capture zone of an event");
        this.plugin = plugin;
        this.aliases = new String[]{"setcapturezone", "setcap", "setcappoint", "setcapturepoint", "setcappoint"};
        this.permission = "hcf.command.event.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <eventName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set KOTH arena capture points");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        WorldEditPlugin worldEdit = plugin.getWorldEdit();

        if (worldEdit == null) {
            sender.sendMessage(ChatColor.RED + "WorldEdit must be installed to set KOTH capture points.");
            return true;
        }

        Selection selection = worldEdit.getSelection((Player) sender);

        if (selection == null) {
            sender.sendMessage(ChatColor.RED + "You must make a WorldEdit selection to do this.");
            return true;
        }

        if (selection.getWidth() < CaptureZone.MINIMUM_SIZE_AREA || selection.getLength() < CaptureZone.MINIMUM_SIZE_AREA) {
            sender.sendMessage(ChatColor.RED + "Capture zones must be at least " + CaptureZone.MINIMUM_SIZE_AREA + 'x' + CaptureZone.MINIMUM_SIZE_AREA + '.');
            return true;
        }

        Faction faction = plugin.getFactionManager().getFaction(args[1]);

        if (!(faction instanceof CapturableFaction)) {
            sender.sendMessage(ChatColor.RED + "There is not a capturable faction named '" + args[1] + "'.");
            return true;
        }

        CapturableFaction capturableFaction = (CapturableFaction) faction;
        Collection<Claim> claims = capturableFaction.getClaims();

        if (claims.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Capture zones can only be inside the event claim.");
            return true;
        }

        Claim claim = new Claim(faction, selection.getMinimumPoint(), selection.getMaximumPoint());

        World world = claim.getWorld();
        int minimumX = claim.getMinimumX();
        int maximumX = claim.getMaximumX();

        int minimumZ = claim.getMinimumZ();
        int maximumZ = claim.getMaximumZ();

        FactionManager factionManager = plugin.getFactionManager();
        for (int x = minimumX; x <= maximumX; x++) {
            for (int z = minimumZ; z <= maximumZ; z++) {
                Faction factionAt = factionManager.getFactionAt(world, x, z);
                if (factionAt != capturableFaction) {
                    sender.sendMessage(ChatColor.RED + "Capture zones can only be inside the event claim.");
                    return true;
                }
            }
        }

        CaptureZone captureZone;
        if (capturableFaction instanceof ConquestFaction) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + ' ' + getName() + ' ' + faction.getName() + " <red|blue|green|yellow>");
                return true;
            }

            ConquestFaction conquestFaction = (ConquestFaction) capturableFaction;
            ConquestFaction.ConquestZone conquestZone = ConquestFaction.ConquestZone.getByName(args[2]);
            if (conquestZone == null) {
                sender.sendMessage(ChatColor.RED + "There is no conquest zone named '" + args[2] + "'.");
                sender.sendMessage(ChatColor.RED + "Did you mean?: " + HCF.COMMA_JOINER.join(ConquestFaction.ConquestZone.getNames()));
                return true;
            }

            captureZone = new CaptureZone(conquestZone.getName(), conquestZone.getColor().toString(), claim, ConquestTracker.DEFAULT_CAP_MILLIS);
            conquestFaction.setZone(conquestZone, captureZone);
        } else if (capturableFaction instanceof KothFaction) {
            ((KothFaction) capturableFaction).setCaptureZone(captureZone = new CaptureZone(capturableFaction.getName(), claim, KothTracker.DEFAULT_CAP_MILLIS));
        } else {
            sender.sendMessage(ChatColor.RED + "Can only set capture zones for Conquest or KOTH factions.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Set capture zone " + captureZone.getDisplayName() + ChatColor.YELLOW + " for faction " + faction.getName() + ChatColor.YELLOW + '.');
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 2:
                return plugin.getFactionManager().getFactions().stream().filter(faction -> faction instanceof EventFaction).map(Faction::getName).collect(Collectors.toList());
            case 3:
                Faction faction = plugin.getFactionManager().getFaction(args[1]);
                if (faction instanceof ConquestFaction) {
                    ConquestFaction.ConquestZone zones[] = ConquestFaction.ConquestZone.values();
                    List<String> results = new ArrayList<>(zones.length);
                    for (ConquestFaction.ConquestZone zone : zones) {
                        results.add(zone.name());
                    }

                    return results;
                } else {
                    return Collections.emptyList();
                }
            default:
                return Collections.emptyList();
        }
    }
}
