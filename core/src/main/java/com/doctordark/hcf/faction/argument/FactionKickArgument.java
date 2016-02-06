package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionKickArgument extends CommandArgument {

    private final HCF plugin;

    public FactionKickArgument(HCF plugin) {
        super("kick", "Kick a player from the faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"kickmember", "kickplayer"};
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can kick from a faction.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        if (playerFaction.isRaidable() && !plugin.getEotwHandler().isEndOfTheWorld()) {
            sender.sendMessage(ChatColor.RED + "You cannot kick players whilst your faction is raidable.");
            return true;
        }

        FactionMember targetMember = playerFaction.getMember(args[1]);

        if (targetMember == null) {
            sender.sendMessage(ChatColor.RED + "Your faction does not have a member named '" + args[1] + "'.");
            return true;
        }

        Role selfRole = playerFaction.getMember(player.getUniqueId()).getRole();

        if (selfRole == Role.MEMBER) {
            sender.sendMessage(ChatColor.RED + "You must be a faction officer to kick members.");
            return true;
        }

        Role targetRole = targetMember.getRole();

        if (targetRole == Role.LEADER) {
            sender.sendMessage(ChatColor.RED + "You cannot kick the faction leader.");
            return true;
        }

        if (targetRole == Role.CAPTAIN && selfRole == Role.CAPTAIN) {
            sender.sendMessage(ChatColor.RED + "You must be a faction leader to kick captains.");
            return true;
        }

        Player onlineTarget = targetMember.toOnlinePlayer();
        if (playerFaction.removeMember(sender, onlineTarget, targetMember.getUniqueId(), true, true)) {
            if (onlineTarget != null) {
                onlineTarget.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "You were kicked from the faction by " + sender.getName() + '.');
            }

            playerFaction.broadcast(plugin.getConfiguration().getRelationColourEnemy() + targetMember.getName() + ChatColor.YELLOW + " has been kicked by " +
                    plugin.getConfiguration().getRelationColourTeammate() + playerFaction.getMember(player).getRole().getAstrix() + sender.getName() + ChatColor.YELLOW + '.');
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            return Collections.emptyList();
        }

        Role memberRole = playerFaction.getMember(player.getUniqueId()).getRole();
        if (memberRole == Role.MEMBER) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        for (UUID entry : playerFaction.getMembers().keySet()) {
            Role targetRole = playerFaction.getMember(entry).getRole();
            if (targetRole == Role.LEADER || (targetRole == Role.CAPTAIN && memberRole != Role.LEADER)) {
                continue;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(entry);
            String targetName = target.getName();
            if (targetName != null && !results.contains(targetName)) {
                results.add(targetName);
            }
        }

        return results;
    }
}
