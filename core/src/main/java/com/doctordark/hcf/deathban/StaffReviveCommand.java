package com.doctordark.hcf.deathban;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import com.doctordark.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class StaffReviveCommand implements CommandExecutor, TabCompleter {

    private final HCF plugin;

    public StaffReviveCommand(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <playerName>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]); //TODO: breaking

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.GOLD + "Player '" + ChatColor.WHITE + args[0] + ChatColor.GOLD + "' not found.");
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        FactionUser factionTarget = HCF.getPlugin().getUserManager().getUser(targetUUID);
        Deathban deathban = factionTarget.getDeathban();

        if (deathban == null || !deathban.isActive()) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is not death-banned.");
            return true;
        }

        factionTarget.removeDeathban();
        Command.broadcastCommandMessage(sender, ChatColor.YELLOW + "Staff revived " + target.getName() + ".");

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
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

        return BukkitUtils.getCompletions(args, results);
    }
}
