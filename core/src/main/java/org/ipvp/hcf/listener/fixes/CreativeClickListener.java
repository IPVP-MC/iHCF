package org.ipvp.hcf.listener.fixes;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;

public class CreativeClickListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlaceCreative(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission("base.command.gamemode")) {
            event.setCancelled(true);
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreakCreative(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission("base.command.gamemode")) {
            event.setCancelled(true);
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player && !humanEntity.hasPermission("base.command.gamemode")) {
            event.setCancelled(true);
            humanEntity.setGameMode(GameMode.SURVIVAL);
        }
    }
}
