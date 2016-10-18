package com.doctordark.hcf.listener;

import com.doctordark.hcf.DateTimeFormats;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class EventSignListener implements Listener {

    private final static String EVENT_SIGN_ITEM_NAME = ChatColor.GOLD + "Event Sign";

    public static ItemStack getEventSign(String playerName, String kothName) {
        ItemStack stack = new ItemStack(Material.SIGN, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(EVENT_SIGN_ITEM_NAME);
        meta.setLore(Lists.newArrayList(
                ChatColor.GREEN + playerName,
                ChatColor.WHITE + "captured by",
                ChatColor.GREEN + kothName,
                DateTimeFormats.DAY_MTH_HR_MIN_SECS.format(System.currentTimeMillis())));
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (isEventSign(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (isEventSign(block)) {
            BlockState state = block.getState();
            Sign sign = (Sign) state;
            ItemStack stack = new ItemStack(Material.SIGN, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(EVENT_SIGN_ITEM_NAME);
            meta.setLore(Arrays.asList(sign.getLines()));
            stack.setItemMeta(meta);

            Player player = event.getPlayer();
            World world = player.getWorld();
            Location blockLocation = block.getLocation();
            if (player.getGameMode() != GameMode.CREATIVE && world.isGameRule("doTileDrops")) {
                world.dropItemNaturally(blockLocation, stack);
            }

            event.setCancelled(true);
            block.setType(Material.AIR);
            state.update();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack stack = event.getItemInHand();
        BlockState state = event.getBlock().getState();
        if (state instanceof Sign && stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().equals(EVENT_SIGN_ITEM_NAME)) {
                Sign sign = (Sign) state;
                List<String> lore = meta.getLore();
                int count = 0;
                for (String loreLine : lore) {
                    sign.setLine(count++, loreLine);
                    if (count == 4) break;
                }

                sign.update();
                sign.setEditable(false);
            }
        }
    }

    private boolean isEventSign(Block block) {
        BlockState state = block.getState();
        if (state instanceof Sign) {
            String[] lines = ((Sign) state).getLines();
            return lines.length > 0 && lines[1] != null && lines[1].equals(ChatColor.WHITE + "captured by");
        }

        return false;
    }
}
