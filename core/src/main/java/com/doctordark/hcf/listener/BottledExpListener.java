package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.util.ExperienceManager;
import com.doctordark.util.JavaUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Listener that stores experience into a Exp Bottle.
 */
public class BottledExpListener implements Listener {

    private static final String BOTTLED_EXP_DISPLAY_NAME = ChatColor.AQUA.toString() + "Bottled Exp";

    private final HCF plugin;

    public BottledExpListener(HCF plugin) {
        this.plugin = plugin;

        // if we don't create a fake custom recipe, players won't be able to craft it.
        Bukkit.addRecipe(new ShapelessRecipe(createExpBottle(1)).addIngredient(Material.GLASS_BOTTLE));
    }

    // Don't ignore cancelled as AIR interactions are cancelled
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfiguration().isBottledExp()) {
            return;
        }

        Action action = event.getAction();
        if (event.hasItem() && (action == Action.RIGHT_CLICK_AIR || (action == Action.RIGHT_CLICK_BLOCK && !event.isCancelled()))) {
            ItemStack stack = event.getItem();
            if (!isBottledExperience(stack)) {
                return;
            }

            ItemMeta meta = stack.getItemMeta();
            List<String> lore = meta.getLore();

            Integer amount = null;
            for (String loreLine : lore) {
                if ((amount = JavaUtils.tryParseInt(ChatColor.stripColor(loreLine).split(" ")[0])) != null) {
                    break;
                }
            }

            if (amount != null) {
                event.setCancelled(true);

                Player player = event.getPlayer();
                int previousLevel = player.getLevel();
                new ExperienceManager(player).changeExp(amount);

                // Play a fancy sound if they earned 5 exp levels.
                if ((player.getLevel() - previousLevel) > 5) {
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
                }

                // If the hand is more than 1, decrease by 1
                // otherwise remove. To prevent user error.
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    player.setItemInHand(new ItemStack(Material.GLASS_BOTTLE, 1));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getHolder() instanceof Player) {
            CraftingInventory inventory = event.getInventory();
            Player player = (Player) inventory.getHolder();
            if (isBottledExperience(inventory.getResult())) {
                int exp = new ExperienceManager(player).getCurrentExp();
                inventory.setResult(exp > 0 ? createExpBottle(exp) : new ItemStack(Material.AIR, 1));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCraftItem(CraftItemEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            if (event.getSlotType() == InventoryType.SlotType.RESULT && isBottledExperience(event.getCurrentItem())) {
                player.setLevel(0);
                player.setExp(0);
            }
        }
    }

    /**
     * Builds a bottled exp {@link ItemStack} based on the experience.
     *
     * @param experience the experience to create with
     * @return the {@link ItemStack}
     */
    private ItemStack createExpBottle(int experience) {
        ItemStack stack = new ItemStack(Material.EXP_BOTTLE, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(BOTTLED_EXP_DISPLAY_NAME);
        meta.setLore(Lists.newArrayList(ChatColor.WHITE.toString() + experience + ChatColor.GOLD + " Experience"));
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Checks if an {@link ItemStack} is bottled exp.
     *
     * @param stack the {@link ItemStack} to check
     * @return true if is bottled exp
     */
    private boolean isBottledExperience(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = stack.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().equals(BOTTLED_EXP_DISPLAY_NAME);
    }
}