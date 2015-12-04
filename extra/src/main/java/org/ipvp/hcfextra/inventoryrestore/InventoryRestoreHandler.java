package org.ipvp.hcfextra.inventoryrestore;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryRestoreHandler implements CommandExecutor, TabCompleter, Listener {

    private final Map<UUID, InventoryState> lastDeathLocation = new HashMap<>();

    private static class InventoryState {

        @Getter
        private final ItemStack[] contents;

        @Getter
        private final ItemStack[] armour;

        public InventoryState(ItemStack[] contents, ItemStack[] armour) {
            this.contents = InventoryRestoreHandler.deepClone(contents);
            this.armour = InventoryRestoreHandler.deepClone(armour);
        }

        public InventoryState(PlayerInventory inventory) {
            this(inventory.getContents(), inventory.getArmorContents());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        this.lastDeathLocation.put(player.getUniqueId(), new InventoryState(player.getInventory()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <playerName>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <playerName>");
            return true;
        }

        InventoryState state = this.lastDeathLocation.get(target.getUniqueId());

        if (state == null) {
            sender.sendMessage(ChatColor.RED + target.getName() + " has not died since the server has been up.");
            return true;
        }

        PlayerInventory inventory = target.getInventory();
        inventory.setContents(state.getContents());
        inventory.setArmorContents(state.getArmour());

        target.sendMessage(ChatColor.YELLOW + sender.getName() + " has restored your last death inventory.");
        sender.sendMessage(ChatColor.YELLOW + "You have restored " + target.getName() + "'s last death inventory.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    private static ItemStack[] deepClone(ItemStack[] origin) {
        ItemStack[] cloned = new ItemStack[origin.length];
        for (int i = 0; i < origin.length; ++i) {
            ItemStack next = origin[i];
            cloned[i] = next == null ? null : next.clone();
        }

        return cloned;
    }
}
