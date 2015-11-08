package com.doctordark.hcf.listener;

import com.doctordark.hcf.ConfigurationService;
import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Listener that alerts {@link Player}s when Diamonds have been uncovered.
 */
public class FoundDiamondsListener implements Listener {

    private static final String NOTIFICATION_PERMISSION = "hcf.founddiamonds.alert";
    private static final Material SEARCH_TYPE = Material.DIAMOND_ORE; //TODO: -> Diamond_ore
    private static final int SEARCH_RADIUS = 3;

    private final Set<String> foundLocations = new HashSet<>();
    private final HCF plugin;

    public FoundDiamondsListener(HCF plugin) {
        this.plugin = plugin;
        if (!ConfigurationService.FOUND_DIAMONDS_ALERTS) {
            plugin.getLogger().info("FoundDiamondsLister won't be enabled as it is disabled in configuration.");
            Bukkit.getScheduler().runTask(plugin, () -> HandlerList.unregisterAll(this));// run the task later so we know it was registered before unregistering.
        } else foundLocations.addAll(plugin.getConfig().getStringList("registered-diamonds"));
    }

    // Prevent players to fake extra diamonds mined.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getType() == SEARCH_TYPE) {
                foundLocations.add(block.getLocation().toString());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == SEARCH_TYPE) {
            foundLocations.add(block.getLocation().toString());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (ConfigurationService.FOUND_DIAMONDS_ALERTS) {
            return;
        }

        // Ignore players in creative mode.
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        if (block.getType() == SEARCH_TYPE && foundLocations.add(blockLocation.toString())) {
            int count = 1; // start at 1 as they had already broke one.
            for (int x = -SEARCH_RADIUS; x < SEARCH_RADIUS; x++) {
                for (int y = -SEARCH_RADIUS; y < SEARCH_RADIUS; y++) {
                    for (int z = -SEARCH_RADIUS; z < SEARCH_RADIUS; z++) {
                        Block otherBlock = blockLocation.clone().add(x, y, z).getBlock();
                        if (!otherBlock.equals(block) && otherBlock.getType() == SEARCH_TYPE && foundLocations.add(otherBlock.getLocation().toString())) {
                            count++;
                        }
                    }
                }
            }

            String message = ChatColor.AQUA + player.getName() + ChatColor.BLUE + " has found " + ChatColor.AQUA + count + " diamond" + (count == 1 ? "" : "s") + ChatColor.BLUE + '.';
            for (Permissible permissible : Bukkit.getPluginManager().getPermissionSubscriptions(NOTIFICATION_PERMISSION)) {
                if (permissible instanceof Player) {
                    ((Player) permissible).sendMessage(message);
                }
            }
        }
    }

    public void saveConfig() {
        plugin.getConfig().set("registered-diamonds", new ArrayList<>(foundLocations));
        plugin.saveConfig();
    }
}
