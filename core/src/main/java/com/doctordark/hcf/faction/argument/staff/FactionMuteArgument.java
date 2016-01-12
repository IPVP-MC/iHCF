package com.doctordark.hcf.faction.argument.staff;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionMuteArgument extends CommandArgument {

    private final HCF plugin;

    public FactionMuteArgument(HCF plugin) {
        super("mute", "Mutes every member in this faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <factionName> <time:(e.g. 1h2s)> <reason>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);

        if (!(faction instanceof PlayerFaction)) {
            sender.sendMessage(ChatColor.RED + "Player faction named or containing member with IGN or UUID " + args[1] + " not found.");
            return true;
        }

        PlayerFaction playerFaction = (PlayerFaction) faction;
        String extraArgs = HCF.SPACE_JOINER.join(Arrays.copyOfRange(args, 2, args.length));
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        for (UUID uuid : playerFaction.getMembers().keySet()) {
            String commandLine = "mute " + uuid.toString() + " " + extraArgs;
            sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Executing " + ChatColor.RED + commandLine);
            console.getServer().dispatchCommand(sender, commandLine);
        }

        sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Executed mute action on faction " + playerFaction.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? null : Collections.emptyList();
    }
}
