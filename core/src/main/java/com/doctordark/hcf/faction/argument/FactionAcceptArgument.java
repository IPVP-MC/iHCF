package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.struct.ChatChannel;
import com.doctordark.hcf.faction.struct.Relation;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Faction argument used to accept invitations from {@link Faction}s.
 */
public class FactionAcceptArgument extends CommandArgument {

    private final HCF plugin;

    public FactionAcceptArgument(HCF plugin) {
        super("accept", "Accept a join request from an existing faction.", new String[]{"join", "a"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <factionName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getFactionManager().getPlayerFaction(player) != null) {
            sender.sendMessage(ChatColor.RED + "You are already in a faction.");
            return true;
        }

        Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);

        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
            return true;
        }

        if (!(faction instanceof PlayerFaction)) {
            sender.sendMessage(ChatColor.RED + "You can only join player factions.");
            return true;
        }

        PlayerFaction targetFaction = (PlayerFaction) faction;

        if (targetFaction.getMembers().size() >= plugin.getConfiguration().getFactionMaxMembers()) {
            sender.sendMessage(faction.getDisplayName(sender) + ChatColor.RED + " is full. Faction limits are at " + plugin.getConfiguration().getFactionMaxMembers() + '.');
            return true;
        }

        if (!targetFaction.isOpen() && !targetFaction.getInvitedPlayerNames().contains(player.getName())) {
            sender.sendMessage(ChatColor.RED + faction.getDisplayName(sender) + ChatColor.RED + " has not invited you.");
            return true;
        }

        if (targetFaction.addMember(player, player, player.getUniqueId(), new FactionMember(player, ChatChannel.PUBLIC, Role.MEMBER))) {
            targetFaction.broadcast(Relation.MEMBER.toChatColour() + sender.getName() + ChatColor.YELLOW + " has joined the faction.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        return plugin.getFactionManager().getFactions().stream().
                filter(faction -> faction instanceof PlayerFaction && ((PlayerFaction) faction).getInvitedPlayerNames().contains(sender.getName())).
                map(faction -> sender.getName()).collect(Collectors.toList());
    }
}
