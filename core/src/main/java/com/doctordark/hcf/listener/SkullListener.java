package com.doctordark.hcf.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void dropPlayerSkull(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer != null && killer.hasPermission("hcf.kill.behead")) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(player.getName());
            skull.setItemMeta(meta);
            event.getDrops().add(skull);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void showPlayerSkullInformation(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && block.getState() instanceof Skull) {
            Skull skull = (Skull) block.getState();
            if (skull.getSkullType() == SkullType.PLAYER) {
                player.sendMessage(ChatColor.YELLOW + "This is the skull of " + skull.getOwner());
            }
        }
    }
}
