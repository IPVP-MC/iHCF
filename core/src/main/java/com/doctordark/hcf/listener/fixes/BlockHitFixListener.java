package com.doctordark.hcf.listener.fixes;

import com.doctordark.util.BukkitUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Prevents players from breaking a break and quickly attacking a
 * player through the block before it regenerates.
 */
public class BlockHitFixListener implements Listener {

    private static final long THRESHOLD = 850L;

    private final TObjectLongMap<UUID> lastInteractTimes = new TObjectLongHashMap<>();

    private static final ImmutableSet<Material> NON_TRANSPARENT_ATTACK_BREAK_TYPES = Sets.immutableEnumSet(
            Material.GLASS,
            Material.STAINED_GLASS,
            Material.STAINED_GLASS_PANE
    );

    private static final ImmutableSet<Material> NON_TRANSPARENT_ATTACK_INTERACT_TYPES = Sets.immutableEnumSet(
            Material.IRON_DOOR_BLOCK,
            Material.IRON_DOOR,
            Material.WOODEN_DOOR,
            Material.WOOD_DOOR,
            Material.TRAP_DOOR,
            Material.FENCE_GATE
    );

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock() && event.getAction() != Action.PHYSICAL && NON_TRANSPARENT_ATTACK_INTERACT_TYPES.contains(event.getClickedBlock().getType())) {
            cancelAttackingMillis(event.getPlayer().getUniqueId(), THRESHOLD);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() && NON_TRANSPARENT_ATTACK_BREAK_TYPES.contains(event.getBlock().getType())) {
            cancelAttackingMillis(event.getPlayer().getUniqueId(), THRESHOLD);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageEvent event) {
        Player attacker = BukkitUtils.getFinalAttacker(event, true);
        if (attacker != null) {
            long lastInteractTime = this.lastInteractTimes.get(attacker.getUniqueId());
            if (lastInteractTime != this.lastInteractTimes.getNoEntryValue() && lastInteractTime - System.currentTimeMillis() > 0L) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent event) {
        this.lastInteractTimes.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        this.lastInteractTimes.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Cancels a {@link Player} from attacking for a certain amount
     * of milliseconds.
     *
     * @param uuid  the uuid of player to cancel for
     * @param delay the milliseconds to cancel for
     */
    public void cancelAttackingMillis(UUID uuid, long delay) {
        this.lastInteractTimes.put(uuid, System.currentTimeMillis() + delay);
    }
}
