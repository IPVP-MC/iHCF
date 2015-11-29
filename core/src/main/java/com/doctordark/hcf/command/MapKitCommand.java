package com.doctordark.hcf.command;

import com.doctordark.hcf.ConfigurationService;
import com.doctordark.hcf.HCF;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.ipvp.util.InventoryUtils;
import org.ipvp.util.ItemBuilder;
import org.ipvp.util.chat.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class MapKitCommand implements CommandExecutor, TabCompleter, Listener {

    private static final String SEPARATOR_LINE = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH.toString() + ChatColor.BOLD.toString() + Strings.repeat('-', 16);

    private Inventory mapkitInventory;

    public MapKitCommand(HCF plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.reloadMapKitInventory();
    }

    private void reloadMapKitInventory() {
        List<ItemStack> items = new ArrayList<>();

        for (Enchantment enchantment : Enchantment.values()) {
            Integer maxLevel = ConfigurationService.ENCHANTMENT_LIMITS.get(enchantment);
            if (maxLevel == null) {
                maxLevel = enchantment.getMaxLevel();
            }

            ItemBuilder builder = new ItemBuilder(Material.ENCHANTED_BOOK);
            builder.displayName(ChatColor.YELLOW + Lang.fromEnchantment(enchantment) + ": " + ChatColor.GREEN + (maxLevel == 0 ? "Disabled" : maxLevel));
            builder.lore(SEPARATOR_LINE, ChatColor.WHITE + "  No Extra Data", SEPARATOR_LINE);
            items.add(builder.build());
        }

        for (PotionType potionType : PotionType.values()) {
            if (potionType == PotionType.WATER) {
                continue;
            }

            Integer maxLevel = ConfigurationService.POTION_LIMITS.get(potionType);
            if (maxLevel == null) {
                maxLevel = potionType.getMaxLevel();
            }

            ItemBuilder builder = new ItemBuilder(new Potion(potionType).toItemStack(1));
            builder.displayName(ChatColor.YELLOW + WordUtils.capitalizeFully(potionType.name().replace('_', ' ')) + ": " + ChatColor.GREEN + (maxLevel == 0 ? "Disabled" : maxLevel));
            builder.lore(SEPARATOR_LINE, ChatColor.WHITE + "  No Extra Data", SEPARATOR_LINE);
            items.add(builder.build());
        }

        this.mapkitInventory = Bukkit.createInventory(null, InventoryUtils.getSafestInventorySize(items.size()), "Map " + ConfigurationService.MAP_NUMBER + " Kit");
        for (ItemStack item : items) {
            this.mapkitInventory.addItem(item);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        ((Player) sender).openInventory(this.mapkitInventory);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (this.mapkitInventory.equals(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        for (HumanEntity viewer : new HashSet<>(this.mapkitInventory.getViewers())) { // copy to prevent co-modification
            viewer.closeInventory();
        }
    }
}
