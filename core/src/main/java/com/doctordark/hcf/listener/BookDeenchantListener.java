package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listener that allows the de-enchanting of books.
 */
public class BookDeenchantListener implements Listener {

    private static final ItemStack EMPTY_BOOK = new ItemStack(Material.BOOK, 1);

    private final HCF plugin;

    public BookDeenchantListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfiguration().isBookDeenchanting()) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.hasItem()) {
            // The player didn't click an enchantment table, Creative players will instantly destroy.
            Player player = event.getPlayer();
            if (event.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE && player.getGameMode() != GameMode.CREATIVE) {
                // The player didn't click with an enchanted book.
                ItemStack stack = event.getItem();
                if (stack != null && stack.getType() == Material.ENCHANTED_BOOK) {
                    ItemMeta meta = stack.getItemMeta();
                    if (meta instanceof EnchantmentStorageMeta) {
                        EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
                        for (Enchantment enchantment : enchantmentStorageMeta.getStoredEnchants().keySet()) {
                            enchantmentStorageMeta.removeStoredEnchant(enchantment);
                        }

                        event.setCancelled(true);
                        player.setItemInHand(EMPTY_BOOK);
                        player.sendMessage(ChatColor.YELLOW + "You reverted this item to its original form.");
                    }
                }
            }
        }
    }
}

