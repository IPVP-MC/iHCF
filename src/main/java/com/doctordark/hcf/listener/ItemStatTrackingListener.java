package com.doctordark.hcf.listener;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStatTrackingListener implements Listener {

    private static final String FORGED_LORE_PREFIX = ChatColor.YELLOW + "Forged by ";
    private static final String BLANK_SPACE = "  ";
    private static final int MAX_KILL_STATS = 3;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onForgeItem(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        if (humanEntity instanceof Player && inventory instanceof AnvilInventory) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                ItemStack stack = event.getCurrentItem();
                if (stack == null || !stack.hasItemMeta()) return;

                Player player = (Player) humanEntity;
                if (player.hasPermission("hcf.showforged")) {
                    ItemMeta meta = stack.getItemMeta();
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(2);
                    if (lore.isEmpty() || !lore.get(0).equals(BLANK_SPACE)) {
                        lore.add(0, BLANK_SPACE);
                    }

                    lore.add(1, FORGED_LORE_PREFIX + ChatColor.GREEN + humanEntity.getName());
                    meta.setLore(lore);
                    stack.setItemMeta(meta);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        for (ItemStack drop : player.getInventory().getArmorContents()) {
            if (EnchantmentTarget.ARMOR.includes(drop)) {
                addDeathLore(drop, player, killer);
            }
        }

        if (killer != null) {
            ItemStack stack = killer.getItemInHand();
            if (stack != null && EnchantmentTarget.WEAPON.includes(stack)) {
                addDeathLore(stack, player, killer);
            }
        }
    }

    /**
     * Adds the lore to a {@link ItemStack} for death statistics.
     *
     * @param stack  the {@link ItemStack} to add to
     * @param player the {@link Player} who died
     * @param killer the {@link Player} killer
     */
    private void addDeathLore(ItemStack stack, Player player, Player killer) {
        ItemMeta meta = stack.getItemMeta();

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(2);
        if (lore.isEmpty() || !lore.get(0).equals(BLANK_SPACE)) {
            lore.add(0, BLANK_SPACE);
        }

        int index = lore.size() <= 1 || !lore.get(1).startsWith(FORGED_LORE_PREFIX) ? 1 : 2;
        lore.add(index, ChatColor.GREEN + player.getName() + ChatColor.YELLOW + ' ' + (killer != null ? "slain by " + ChatColor.GREEN + killer.getName() : "died"));

        meta.setLore(lore.subList(0, Math.min((MAX_KILL_STATS + 2), lore.size())));
        stack.setItemMeta(meta);
    }
}
