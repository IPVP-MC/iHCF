package org.ipvp.hcf.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class AutoSmeltOreListener implements Listener {

    private static final String AUTO_SMELT_ORE_PERMISSION = "hcf.autosmeltore";

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE && player.hasPermission(AUTO_SMELT_ORE_PERMISSION)) {
            ItemStack stack = player.getItemInHand();
            if (stack != null && stack.getType() != Material.AIR && !stack.containsEnchantment(Enchantment.SILK_TOUCH)) {
                Block block = event.getBlock();
                Material dropType;
                switch (block.getType()) {
                    case IRON_ORE:
                        dropType = Material.IRON_INGOT;
                        break;
                    case GOLD_ORE:
                        dropType = Material.GOLD_INGOT;
                        break;
                    default:
                        return;
                }

                Location location = block.getLocation();
                /*int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();*/
                World world = location.getWorld();

                // Using the Bukkit API doesn't do this as intended ;/

                ItemStack drop = new ItemStack(dropType, 1);
                world.dropItemNaturally(location, drop);

                //net.minecraft.server.v1_7_R4.World nmsWorld = ((CraftWorld) world).getHandle();
                //net.minecraft.server.v1_7_R4.Block nmsBlock = nmsWorld.getType(x, y, z);
                //TODO: exp drops

                // Manually handle the cancellation of event.
                block.setType(Material.AIR);
                block.getState().update();
            }
        }
    }
}
