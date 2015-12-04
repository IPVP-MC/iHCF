package org.ipvp.hcfextra.command;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EndportalHandler implements CommandExecutor, TabCompleter, Listener {

    private final Map<UUID, Location> firstSelections = new HashMap<>();

    private static final ItemStack END_PORTAL_BUILDER_STACK;

    static {
        END_PORTAL_BUILDER_STACK = new ItemStack(Material.GOLD_SWORD, 1);
        ItemMeta meta = END_PORTAL_BUILDER_STACK.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "End Portal Builder");
        meta.setLore(Lists.newArrayList(
                "Left Click - Select first position",
                "Right Click - Select second position (filling up to first)")
        );

        END_PORTAL_BUILDER_STACK.setItemMeta(meta);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can have inventories.");
            return true;
        }

        Player player = (Player) sender;

        if (player.getInventory().contains(END_PORTAL_BUILDER_STACK)) {
            sender.sendMessage(ChatColor.RED + "Already have an end portal builder in inventory.");
            return true;
        }

        player.getInventory().addItem(END_PORTAL_BUILDER_STACK);

        sender.sendMessage(ChatColor.YELLOW + "Added an end portal builder to inventory");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack stack = event.getPlayer().getItemInHand();
        if (stack != null && stack.isSimilar(END_PORTAL_BUILDER_STACK)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack stack = event.getPlayer().getItemInHand();
        if (stack != null && stack.isSimilar(END_PORTAL_BUILDER_STACK)) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getItem().isSimilar(END_PORTAL_BUILDER_STACK) && event.hasBlock()) {
            Action action = event.getAction();
            boolean leftClick;
            switch (action) {
                case LEFT_CLICK_BLOCK:
                    leftClick = true;
                    break;
                case RIGHT_CLICK_BLOCK:
                    leftClick = false;
                    break;
                default:
                    return;
            }

            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock.getType() != Material.ENDER_PORTAL_FRAME) {
                player.sendMessage(ChatColor.RED + "You must select an End Portal Frame only with this tool.");
                return;
            }

            Location clickedLocation = clickedBlock.getLocation();
            if (leftClick) {
                this.firstSelections.put(player.getUniqueId(), clickedLocation.clone());
                player.sendMessage(ChatColor.YELLOW + "Set your first End Portal filler position at " +
                        ChatColor.AQUA + "(" + clickedLocation.getBlockX() + ", " + clickedLocation.getBlockY() + ", " + clickedLocation.getBlockZ() + ")" + ChatColor.YELLOW + ".");
            } else {
                Location first = this.firstSelections.remove(player.getUniqueId());

                if (first == null) {
                    player.sendMessage(ChatColor.RED + "Set your first location first by right clicking an End Portal Frame.");
                    return;
                }

                if (!first.getWorld().equals(clickedLocation.getWorld())) {
                    player.sendMessage(ChatColor.RED + "First location must be in the same world as the clicked block.");
                    return;
                }

                if (Math.abs(first.getBlockX() - clickedBlock.getX()) > 5) {
                    player.sendMessage(ChatColor.RED + "Maximum End Portal Builder radius vertically is 5.");
                    return;
                }

                if (first.getBlockY() != clickedBlock.getY()) {
                    player.sendMessage(ChatColor.RED + "Clicked block is not on the same y level as first location.");
                    return;
                }

                World world = first.getWorld();
                int minX = Math.min(first.getBlockX(), clickedBlock.getX());
                int maxX = Math.max(first.getBlockX(), clickedBlock.getX());
                int minZ = Math.min(first.getBlockZ(), clickedBlock.getZ());
                int maxZ = Math.max(first.getBlockZ(), clickedBlock.getZ());
                int y = first.getBlockY(), count = 0;
                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        Block next = world.getBlockAt(x, y, z);
                        if (next != null && next.getType() == Material.AIR) {
                            count++;
                            next.setType(Material.ENDER_PORTAL);
                        }
                    }
                }

                player.sendMessage(ChatColor.YELLOW + "End portal filled between first and second selection (" + count + ") blocks replaced.");
            }
        }
    }
}
