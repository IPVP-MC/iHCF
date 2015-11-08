package com.doctordark.hcf.faction.argument.staff;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.FactionManager;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.JavaUtils;
import com.doctordark.util.command.CommandArgument;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionSetDtrRegenArgument extends CommandArgument {

    private final HCF plugin;

    public FactionSetDtrRegenArgument(HCF plugin) {
        super("setdtrregen", "Sets the DTR cooldown of a faction.", new String[]{"setdtrregeneration"});
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName|factionName> <newRegen>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        long newRegen = JavaUtils.parse(args[2]);

        if (newRegen < 0L) {
            sender.sendMessage(ChatColor.RED + "Faction DTR regeneration duration cannot be negative.");
            return true;
        }

        if (newRegen > FactionManager.MAX_DTR_REGEN_MILLIS) {
            sender.sendMessage(ChatColor.RED + "Cannot set factions DTR regen above " + FactionManager.MAX_DTR_REGEN_WORDS + ".");
            return true;
        }

        Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);

        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
            return true;
        }

        if (!(faction instanceof PlayerFaction)) {
            sender.sendMessage(ChatColor.RED + "This type of faction does not use DTR.");
            return true;
        }

        PlayerFaction playerFaction = (PlayerFaction) faction;
        long previousRegenRemaining = playerFaction.getRemainingRegenerationTime();
        playerFaction.setRemainingRegenerationTime(newRegen);

        Command.broadcastCommandMessage(sender, ChatColor.YELLOW + "Set DTR regen of " + faction.getName() +
                (previousRegenRemaining > 0L ? " from " + DurationFormatUtils.formatDurationWords(previousRegenRemaining, true, true) : "") + " to " +
                DurationFormatUtils.formatDurationWords(newRegen, true, true) + '.');

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        } else if (args[1].isEmpty()) {
            return null;
        } else {
            List<String> results = new ArrayList<>(plugin.getFactionManager().getFactionNameMap().keySet());
            Player senderPlayer = sender instanceof Player ? ((Player) sender) : null;
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Make sure the player can see.
                if (senderPlayer == null || senderPlayer.canSee(player)) {
                    results.add(player.getName());
                }
            }

            return results;
        }
    }
}
