package com.doctordark.hcf.listener.fixes;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.ClaimableFaction;
import com.doctordark.hcf.faction.type.Faction;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;

public class PearlGlitchListener implements Listener {

    private final ImmutableSet<Material> blockedPearlTypes = Sets.immutableEnumSet(
            Material.THIN_GLASS,
            Material.IRON_FENCE,
            Material.FENCE,
            Material.NETHER_FENCE,
            Material.FENCE_GATE,
            Material.ACACIA_STAIRS,
            Material.BIRCH_WOOD_STAIRS,
            Material.BRICK_STAIRS,
            Material.COBBLESTONE_STAIRS,
            Material.DARK_OAK_STAIRS,
            Material.JUNGLE_WOOD_STAIRS,
            Material.NETHER_BRICK_STAIRS,
            Material.QUARTZ_STAIRS,
            Material.SANDSTONE_STAIRS,
            Material.SMOOTH_STAIRS,
            Material.SPRUCE_WOOD_STAIRS,
            Material.WOOD_STAIRS,
            Material.WOOD_STEP,
            Material.WOOD_DOUBLE_STEP,
            Material.STEP,
            Material.DOUBLE_STEP
    );

    private final HCF plugin;

    public PearlGlitchListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasItem() && event.getItem().getType() == Material.ENDER_PEARL) {
            Block block = event.getClickedBlock();
            // Don't prevent opening chests, etc, as these won't throw the Enderpearls anyway
            if (block.getType().isSolid() && !(block.getState() instanceof InventoryHolder)) {
                Faction factionAt = HCF.getPlugin().getFactionManager().getFactionAt(block.getLocation());
                if (factionAt instanceof ClaimableFaction) {
                    event.setCancelled(true);
                    Player player = event.getPlayer();
                    player.setItemInHand(event.getItem()); // required to update Enderpearl count
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPearlClip(PlayerTeleportEvent event) {
        if (!plugin.getConfiguration().isEnderpearlGlitchingEnabled()) {
            return;
        }

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Location to = event.getTo();
            if (blockedPearlTypes.contains(to.getBlock().getType())) {
                Player player = event.getPlayer();
                boolean refund = plugin.getConfiguration().isEnderpearlGlitchingRefund();

                player.sendMessage(ChatColor.YELLOW + "Pearl glitching detected" + (refund ? ", used Enderpearl has been refunded" : "") + ".");
                if (refund) plugin.getTimerManager().getEnderPearlTimer().refund(player);

                event.setCancelled(true);
                return;
            }

            to.setX(to.getBlockX() + 0.5);
            to.setZ(to.getBlockZ() + 0.5);
            event.setTo(to);
        }
    }
}
