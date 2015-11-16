package org.ipvp.hcf.faction.argument.staff;

import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.FactionMember;
import org.ipvp.hcf.faction.struct.Role;
import org.ipvp.hcf.faction.type.PlayerFaction;

import java.util.Collections;
import java.util.List;

public class FactionForcePromoteArgument extends CommandArgument {

    private final HCF plugin;

    public FactionForcePromoteArgument(HCF plugin) {
        super("forcepromote", "Forces the promotion status of a player.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        PlayerFaction playerFaction = plugin.getFactionManager().getContainingPlayerFaction(args[1]);

        if (playerFaction == null) {
            sender.sendMessage(ChatColor.RED + "Faction containing member with IGN or UUID " + args[1] + " not found.");
            return true;
        }

        FactionMember factionMember = playerFaction.getMember(args[1]);

        if (factionMember == null) {
            sender.sendMessage(ChatColor.RED + "Faction containing member with IGN or UUID " + args[1] + " not found.");
            return true;
        }

        if (factionMember.getRole() != Role.MEMBER) {
            sender.sendMessage(ChatColor.RED + factionMember.getName() + " is already a " + factionMember.getRole().getName() + '.');
            return true;
        }

        factionMember.setRole(Role.CAPTAIN);
        playerFaction.broadcast(ChatColor.GOLD.toString() + ChatColor.BOLD + sender.getName() + " has been forcefully assigned as a captain.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? null : Collections.emptyList();
    }
}
