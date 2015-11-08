package com.doctordark.hcf.faction.argument.subclaim;

import com.doctordark.hcf.faction.claim.ClaimHandler;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.List;

/**
 * Faction subclaim argument used to receive the Subclaim Wand.
 */
public class FactionSubclaimStartArgument extends CommandArgument {

    public FactionSubclaimStartArgument() {
        super("start", "Receive the subclaim wand", new String[]{"begin", "claim", "wand"});
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

        PlayerInventory inventory = ((Player) sender).getInventory();

        if (inventory.contains(ClaimHandler.SUBCLAIM_WAND)) {
            sender.sendMessage(ChatColor.RED + "You already have a subclaim wand in your inventory.");
            return true;
        }

        if (inventory.contains(ClaimHandler.CLAIM_WAND)) {
            sender.sendMessage(ChatColor.RED + "You cannot have a subclaim wand whilst you have a claiming wand in your inventory.");
            return true;
        }

        if (!inventory.addItem(ClaimHandler.SUBCLAIM_WAND).isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Your inventory is full.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Subclaim wand added to inventory. Read the item to understand how to create a subclaim.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
