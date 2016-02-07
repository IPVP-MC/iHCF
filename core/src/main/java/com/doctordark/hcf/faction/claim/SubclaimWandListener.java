package com.doctordark.hcf.faction.claim;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.visualise.VisualBlock;
import com.doctordark.hcf.visualise.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class SubclaimWandListener implements Listener {

    private final HCF plugin;

    public SubclaimWandListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        // They didn't use a claiming wand for this action, so ignore.
        if (action == Action.PHYSICAL || !event.hasItem() || !isSubclaimWand(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Clearing the claim selection of player.
        if (action == Action.RIGHT_CLICK_AIR) {
            plugin.getClaimHandler().clearClaimSelection(player);
            player.setItemInHand(new ItemStack(Material.AIR, 1));
            player.sendMessage(ChatColor.RED + "You have cleared your subclaim selection.");
            return;
        }

        if (event.isCancelled()) return;
        /*if ((action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) && player.isSneaking()) {
            //TODO: Possibly open an Anvil to make a name for the Subclaim?
        }*/

        // Setting the positions for the claim selection;
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            Location blockLocation = event.getClickedBlock().getLocation();

            // Don't hoe the soil block.
            if (action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }

            if (plugin.getClaimHandler().canSubclaimHere(player, blockLocation)) {
                ClaimSelection revert;
                ClaimSelection claimSelection = plugin.getClaimHandler().claimSelectionMap.putIfAbsent(uuid, revert = new ClaimSelection(blockLocation.getWorld()));
                if (claimSelection == null) claimSelection = revert;

                Location oldPosition;
                Location opposite;
                int selectionId;
                switch (action) {
                    case LEFT_CLICK_BLOCK:
                        oldPosition = claimSelection.getPos1();
                        opposite = claimSelection.getPos2();
                        selectionId = 1;
                        break;
                    case RIGHT_CLICK_BLOCK:
                        oldPosition = claimSelection.getPos2();
                        opposite = claimSelection.getPos1();
                        selectionId = 2;
                        break;
                    default:
                        return; // This should never happen.
                }

                // Prevent players clicking in the same spot twice.
                int blockX = blockLocation.getBlockX();
                int blockZ = blockLocation.getBlockZ();
                if (oldPosition != null && blockX == oldPosition.getBlockX() && blockZ == oldPosition.getBlockZ()) {
                    return;
                }

                // Allow at least 1 tick before players can update one of the positions to prevent lag/visual glitches with delayed task below.
                if ((System.currentTimeMillis() - claimSelection.getLastUpdateMillis()) <= ClaimHandler.PILLAR_BUFFER_DELAY_MILLIS) {
                    return;
                }

                if (opposite != null && (Math.abs(opposite.getBlockX() - blockX) <= ClaimHandler.MIN_SUBCLAIM_RADIUS || Math.abs(opposite.getBlockZ() - blockZ) <= ClaimHandler.MIN_SUBCLAIM_RADIUS)) {
                    player.sendMessage(ChatColor.RED + "Subclaim selections must be at least " + ClaimHandler.MIN_SUBCLAIM_RADIUS + 'x' + ClaimHandler.MIN_SUBCLAIM_RADIUS + " blocks.");
                    return;
                }

                if (oldPosition != null) {
                    plugin.getVisualiseHandler().clearVisualBlocks(player, VisualType.CREATE_CLAIM_SELECTION, new Predicate<VisualBlock>() {
                        @Override
                        public boolean test(VisualBlock visualBlock) {
                            Location location = visualBlock.getLocation();
                            return location.getBlockX() == oldPosition.getBlockX() && location.getBlockZ() == oldPosition.getBlockZ();
                        }
                    });
                }

                if (selectionId == 1) claimSelection.setPos1(blockLocation);
                if (selectionId == 2) claimSelection.setPos2(blockLocation);

                player.sendMessage(ChatColor.GREEN + "Set the location of subclaim selection " + ChatColor.YELLOW + selectionId + ChatColor.GREEN + " to: " +
                        ChatColor.GOLD + '(' + ChatColor.YELLOW + blockX + ", " + blockZ + ChatColor.GOLD + ')');

                final int blockY = block.getY();
                final int maxHeight = player.getWorld().getMaxHeight();
                final List<Location> locations = new ArrayList<>(maxHeight);
                for (int i = blockY; i < maxHeight; i++) {
                    Location other = blockLocation.clone();
                    other.setY(i);
                    locations.add(other);
                }

                // Generate the new claiming pillar a tick later as right clicking using this
                // event doesn't update the bottom block clicked occasionally.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getVisualiseHandler().generate(player, locations, VisualType.CREATE_CLAIM_SELECTION, true);
                    }
                }.runTask(plugin);
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isSubclaimWand(event.getPlayer().getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isSubclaimWand(player.getItemInHand())) {
                player.setItemInHand(new ItemStack(Material.AIR, 1));
                plugin.getClaimHandler().clearClaimSelection(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent event) {
        event.getPlayer().getInventory().remove(ClaimHandler.SUBCLAIM_WAND);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().getInventory().remove(ClaimHandler.SUBCLAIM_WAND);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (isSubclaimWand(item.getItemStack())) {
            item.remove();
            plugin.getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        if (isSubclaimWand(item.getItemStack())) {
            item.remove();
            plugin.getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    // Prevents dropping Claiming Wands on death.
    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().remove(ClaimHandler.SUBCLAIM_WAND)) {
            plugin.getClaimHandler().clearClaimSelection(event.getEntity());
        }
    }

    // Doesn't get called when opening own inventory.
    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            if (player.getInventory().contains(ClaimHandler.SUBCLAIM_WAND)) {
                plugin.getClaimHandler().clearClaimSelection(player);
            }
            player.getInventory().remove(ClaimHandler.SUBCLAIM_WAND);
        }
    }

    /**
     * Checks if an {@link ItemStack} is a Subclaim Wand.
     *
     * @param stack the {@link ItemStack} to check
     * @return true if the {@link ItemStack} is a subclaim wand
     */
    public boolean isSubclaimWand(ItemStack stack) {
        return stack != null && stack.isSimilar(ClaimHandler.SUBCLAIM_WAND);
    }
}