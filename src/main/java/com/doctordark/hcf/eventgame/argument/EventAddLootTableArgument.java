package com.doctordark.hcf.eventgame.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.eventgame.crate.EventKey;
import com.doctordark.util.InventoryUtils;
import com.doctordark.util.JavaUtils;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An {@link CommandArgument} used to add a loot table for an {@link EventType}.
 */
public class EventAddLootTableArgument extends CommandArgument {

    private final HCF plugin;

    public EventAddLootTableArgument(HCF plugin) {
        super("addloottable", "Adds another loot table for an event type");
        this.plugin = plugin;
        this.permission = "hcf.command.event.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <eventType> [size (multiple of 9)]";
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

        EventType eventType = EventType.getByDisplayName(args[1]);

        if (eventType == null) {
            sender.sendMessage(ChatColor.RED + "There is not an event type named " + args[1] + '.');
            return true;
        }

        Integer size = JavaUtils.tryParseInt(args[2]);
        if (size == null) {
            size = InventoryUtils.MAXIMUM_SINGLE_CHEST_SIZE;
        } else if (size % InventoryUtils.DEFAULT_INVENTORY_WIDTH != 0) {
            sender.sendMessage(ChatColor.RED + "Inventory size must be a multiple of " + InventoryUtils.DEFAULT_INVENTORY_WIDTH + '.');
            return true;
        } else if (size < InventoryUtils.MINIMUM_INVENTORY_SIZE) {
            sender.sendMessage(ChatColor.RED + "Inventory size must be at least " + InventoryUtils.MINIMUM_INVENTORY_SIZE + '.');
            return true;
        } else if (size > InventoryUtils.MAXIMUM_DOUBLE_CHEST_SIZE) {
            sender.sendMessage(ChatColor.RED + "Inventory size must be at most " + InventoryUtils.MAXIMUM_DOUBLE_CHEST_SIZE + '.');
            return true;
        }

        EventKey eventKey = plugin.getKeyManager().getEventKey();
        Collection<Inventory> inventories = eventKey.getInventories(eventType);
        int newAmount = inventories.size() + 1;
        inventories.add(Bukkit.createInventory(null, size, eventType.getDisplayName() + " Loot " + newAmount));
        sender.sendMessage(ChatColor.YELLOW + "Created a new key inventory of size " + size + " for event " + eventType.getDisplayName() + ". There are now " + newAmount + " inventories.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }

        EventType[] eventTypes = EventType.values();
        List<String> results = new ArrayList<>(eventTypes.length);
        for (EventType eventType : eventTypes) {
            results.add(eventType.name());
        }

        return results;
    }
}
