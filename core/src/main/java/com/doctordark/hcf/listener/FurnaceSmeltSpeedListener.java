package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FurnaceSmeltSpeedListener implements Listener {

    private final HCF plugin;

    public FurnaceSmeltSpeedListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            double multiplier = plugin.getConfiguration().getFurnaceCookSpeedMultiplier();
            if (multiplier == 1.0) {
                return;
            }

            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            if (state instanceof Furnace) {
                ((Furnace) state).setCookSpeedMultiplier(plugin.getConfiguration().getFurnaceCookSpeedMultiplier());
            }
        }
    }
}
