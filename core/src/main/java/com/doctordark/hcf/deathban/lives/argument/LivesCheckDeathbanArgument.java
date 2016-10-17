package com.doctordark.hcf.deathban.lives.argument;

import com.doctordark.hcf.DateTimeFormats;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.deathban.Deathban;
import com.doctordark.hcf.user.FactionUser;
import com.doctordark.util.command.CommandArgument;
import com.google.common.base.Strings;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An {@link CommandArgument} used to check the {@link Deathban} of a {@link Player}.
 */
public class LivesCheckDeathbanArgument extends CommandArgument {

    private final HCF plugin;

    public LivesCheckDeathbanArgument(HCF plugin) {
        super("checkdeathban", "Check the deathban cause of player");
        this.plugin = plugin;
        this.permission = "hcf.command.lives.argument." + getName();
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

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]); //TODO: breaking

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.GOLD + "Player '" + ChatColor.WHITE + args[1] + ChatColor.GOLD + "' not found.");
            return true;
        }

        Deathban deathban = plugin.getUserManager().getUser(target.getUniqueId()).getDeathban();

        if (deathban == null || !deathban.isActive()) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is not death-banned.");
            return true;
        }

        sender.sendMessage(ChatColor.DARK_AQUA + "Deathban cause of " + target.getName() + '.');
        sender.sendMessage(ChatColor.GRAY + " Time: " + DateTimeFormats.HR_MIN.format(deathban.getCreationMillis()));
        sender.sendMessage(ChatColor.GRAY + " Duration: " + DurationFormatUtils.formatDurationWords(deathban.getInitialDuration(), true, true));

        Location location = deathban.getDeathPoint();
        if (location != null) {
            sender.sendMessage(ChatColor.GRAY + " Location: (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ") - " + location.getWorld().getName());
        }

        sender.sendMessage(ChatColor.GRAY + " Reason: [" + Strings.nullToEmpty(deathban.getReason()) + ChatColor.GREEN + "]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        for (FactionUser factionUser : plugin.getUserManager().getUsers().values()) {
            Deathban deathban = factionUser.getDeathban();
            if (deathban != null && deathban.isActive()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(factionUser.getUserUUID());
                String name = offlinePlayer.getName();
                if (name != null) {
                    results.add(name);
                }
            }
        }

        return results;
    }
}
