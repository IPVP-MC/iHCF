package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.JavaUtils;
import com.doctordark.util.command.CommandArgument;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class FactionRenameArgument extends CommandArgument {

    private static final long FACTION_RENAME_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(15L);
    private static final String FACTION_RENAME_DELAY_WORDS = DurationFormatUtils.formatDurationWords(FACTION_RENAME_DELAY_MILLIS, true, true);

    private final HCF plugin;

    public FactionRenameArgument(HCF plugin) {
        super("rename", "Change the name of your faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"changename", "setname"};
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <newFactionName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can create faction.");
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

        if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
            sender.sendMessage(ChatColor.RED + "You must be a faction leader to edit the name.");
            return true;
        }

        String newName = args[1];

        if (plugin.getConfiguration().getFactionDisallowedNames().contains(newName.toLowerCase())) {
            sender.sendMessage(ChatColor.RED + "'" + newName + "' is a blocked faction name.");
            return true;
        }

        int value = plugin.getConfiguration().getFactionNameMinCharacters();

        if (newName.length() < value) {
            sender.sendMessage(ChatColor.RED + "Faction names must have at least " + value + " characters.");
            return true;
        }

        value = plugin.getConfiguration().getFactionNameMaxCharacters();

        if (newName.length() > value) {
            sender.sendMessage(ChatColor.RED + "Faction names cannot be longer than " + value + " characters.");
            return true;
        }

        if (!JavaUtils.isAlphanumeric(newName)) {
            sender.sendMessage(ChatColor.RED + "Faction names may only be alphanumeric.");
            return true;
        }

        if (plugin.getFactionManager().getFaction(newName) != null) {
            sender.sendMessage(ChatColor.RED + "Faction " + newName + ChatColor.RED + " already exists.");
            return true;
        }

        long difference = (playerFaction.lastRenameMillis - System.currentTimeMillis()) + FACTION_RENAME_DELAY_MILLIS;

        if (!player.isOp() && difference > 0L) {
            player.sendMessage(ChatColor.RED + "There is a faction rename delay of " + FACTION_RENAME_DELAY_WORDS + ". Therefore you need to wait another " +
                    DurationFormatUtils.formatDurationWords(difference, true, true) + " to rename your faction.");

            return true;
        }

        playerFaction.setName(args[1], sender);
        return true;
    }
}
