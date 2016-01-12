package com.doctordark.hcf.faction.argument.subclaim;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.claim.Subclaim;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.command.CommandArgument;
import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FactionSubclaimListArgument extends CommandArgument {

    private final HCF plugin;

    public FactionSubclaimListArgument(HCF plugin) {
        super("list", "List subclaims in this faction", new String[]{"listsubs"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + " subclaim " + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        List<String> subclaimNames = new ArrayList<>();
        for (Claim claim : playerFaction.getClaims()) {
            subclaimNames.addAll(claim.getSubclaims().stream().map(Subclaim::getName).collect(Collectors.toList()));
        }

        if (subclaimNames.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Your faction does not own any subclaims.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Factions' Subclaims (" + subclaimNames.size() + "): " + ChatColor.AQUA + HCF.COMMA_JOINER.join(subclaimNames));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
