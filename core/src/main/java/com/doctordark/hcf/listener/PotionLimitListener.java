package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

/**
 * Listener that prevents the brewing of illegal {@link org.bukkit.potion.PotionEffectType}s.
 */
public class PotionLimitListener implements Listener {

    private static final int EMPTY_BREW_TIME = 400;

    private final HCF plugin;

    public PotionLimitListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBrew(BrewEvent event) {
        if (plugin.isPaperPatch()) {
            // TODO: BROKEN: event.getContents().getContents() unknown results
            if (!testValidity(event.getContents().getContents())) {
                event.setCancelled(true);
                event.getContents().getHolder().setBrewingTime(EMPTY_BREW_TIME);
            }

            return;
        }

        BrewerInventory inventory = event.getContents();
        ItemStack[] contents = inventory.getContents();
        int length = contents.length;
        ItemStack[] cloned = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            ItemStack previous = contents[i];
            cloned[i] = (previous == null ? null : previous.clone());
        }

        BrewingStand stand = inventory.getHolder();
        Bukkit.getScheduler().runTask(HCF.getPlugin(), () -> {
            if (!testValidity(inventory.getContents())) {
                stand.setBrewingTime(EMPTY_BREW_TIME);
                inventory.setContents(cloned);
            }
        });
    }

    private boolean testValidity(ItemStack[] contents) {
        for (ItemStack stack : contents) {
            if (stack != null && stack.getType() == Material.POTION && stack.getDurability() != 0) {
                Potion potion = Potion.fromItemStack(stack);

                // Just to be safe, null check this.
                if (potion == null) {
                    continue;
                }

                // Mundane potions etc, can return a null type
                PotionType type = potion.getType();
                if (type == null) {
                    continue;
                }

                // TODO: More configurable if 33 second splash poison is allowed
                if (type == PotionType.POISON && !potion.hasExtendedDuration() && potion.getLevel() == 1) {
                    continue;
                }

                if (potion.getLevel() > plugin.getConfiguration().getPotionLimit(type)) {
                    return false;
                }
            }
        }

        return true;
    }
}
