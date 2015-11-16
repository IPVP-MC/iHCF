package org.ipvp.hcf.timer.type;

import org.ipvp.hcf.DurationFormatter;
import org.ipvp.hcf.timer.PlayerTimer;
import org.ipvp.hcf.timer.TimerCooldown;
import com.doctordark.util.Config;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutSetSlot;
import net.minecraft.server.v1_7_R4.PlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EnderPearlTimer extends PlayerTimer implements Listener {

    private static final long REFRESH_DELAY_TICKS = 2L;           // time in ticks it will update the remaining time on the Enderpearl.
    private static final long REFRESH_DELAY_TICKS_18 = 20L;       // time in ticks it will update the remaining time on the Enderpearl for a 1.8 client.
    private static final long EXPIRE_SHOW_MILLISECONDS = 1500L;   // time in milliseconds it will show the Enderpearl cooldown is over on the hotbar for

    private final Map<UUID, PearlNameFaker> itemNameFakes = new HashMap<>();
    private final JavaPlugin plugin;

    public EnderPearlTimer(JavaPlugin plugin) {
        super("Enderpearl", TimeUnit.SECONDS.toMillis(15L));
        this.plugin = plugin;
    }

    @Override
    public String getScoreboardPrefix() {
        return ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD;
    }

    @Override
    public void load(Config config) {
        super.load(config);

        // Reapply the fake items for players after reloads.
        Collection<UUID> cooldowned = cooldowns.keySet();
        for (UUID uuid : cooldowned) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                startDisplaying(player);
            }
        }
    }

    @Override
    public void onDisable(Config config) {
        super.onDisable(config);

        // Clear the fake items for players when reloading.
        for (Iterator<PearlNameFaker> iterator = itemNameFakes.values().iterator(); iterator.hasNext(); ) {
            iterator.next().cancel();
            iterator.remove();
        }
    }

    @Override
    public void onExpire(UUID userUUID) {
        super.onExpire(userUUID);
        Player player = Bukkit.getPlayer(userUUID);
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Your " + getDisplayName() + ChatColor.GREEN + " timer has expired. You may now Enderpearl again.");
        }
    }

    @Override
    public TimerCooldown clearCooldown(UUID playerUUID) {
        TimerCooldown runnable = super.clearCooldown(playerUUID);
        if (runnable != null) {
            itemNameFakes.remove(playerUUID);
            return runnable;
        }

        return null;
    }

    @Override
    public void clearCooldown(Player player) {
        this.stopDisplaying(player);
        super.clearCooldown(player);
    }

    public void refund(Player player) {
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
        clearCooldown(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof EnderPearl) {
            EnderPearl enderPearl = (EnderPearl) projectile;
            ProjectileSource source = enderPearl.getShooter();
            if (source instanceof Player) {
                Player shooter = (Player) source;
                long remaining = getRemaining(shooter);
                if (remaining > 0L) {
                    shooter.sendMessage(ChatColor.RED + "You still have a " + getDisplayName() +
                            ChatColor.RED + " cooldown for another " + ChatColor.BOLD + DurationFormatter.getRemaining(remaining, true, false) + ChatColor.RED + '.');

                    event.setCancelled(true);
                    return;
                }

                if (setCooldown(shooter, shooter.getUniqueId(), defaultCooldown, true)) {
                    startDisplaying(shooter);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearCooldown(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        clearCooldown(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PearlNameFaker pearlNameFaker = itemNameFakes.get(player.getUniqueId());
        if (pearlNameFaker != null) {
            int previousSlot = event.getPreviousSlot();
            ItemStack item = player.getInventory().getItem(previousSlot);
            if (item == null) return;

            pearlNameFaker.setFakeItem(((CraftItemStack) item).handle, previousSlot);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            PearlNameFaker pearlNameFaker = itemNameFakes.get(player.getUniqueId());
            if (pearlNameFaker == null) return;
            for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
                if (entry.getKey() == player.getInventory().getHeldItemSlot()) {
                    pearlNameFaker.setFakeItem(CraftItemStack.asNMSCopy(player.getItemInHand()), player.getInventory().getHeldItemSlot());
                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            PearlNameFaker pearlNameFaker = itemNameFakes.get(player.getUniqueId());
            if (pearlNameFaker == null) return;

            // Required to prevent ghost items.
            int heldSlot = player.getInventory().getHeldItemSlot();
            if (event.getSlot() == heldSlot) {
                pearlNameFaker.setFakeItem(CraftItemStack.asNMSCopy(player.getItemInHand()), heldSlot);
            } else if (event.getHotbarButton() == heldSlot) {
                pearlNameFaker.setFakeItem(CraftItemStack.asNMSCopy(event.getCurrentItem()), event.getSlot());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.updateInventory();
                    }
                }.runTask(plugin);
            }
        }
    }

    /**
     * Starts displaying the remaining Enderpearl cooldown on the hotbar.
     *
     * @param player the {@link Player} to display for
     */
    public void startDisplaying(Player player) {
        PearlNameFaker pearlNameFaker;
        if (getRemaining(player) > 0L && itemNameFakes.putIfAbsent(player.getUniqueId(), pearlNameFaker = new PearlNameFaker(this, player)) == null) {
            long ticks = ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion() >= 47 ? REFRESH_DELAY_TICKS_18 : REFRESH_DELAY_TICKS;
            pearlNameFaker.runTaskTimerAsynchronously(plugin, ticks, ticks);
        }
    }

    /**
     * Stop displaying the remaining Enderpearl cooldown on the hotbar.
     *
     * @param player the {@link Player} to stop for
     */
    public void stopDisplaying(Player player) {
        PearlNameFaker pearlNameFaker = itemNameFakes.remove(player.getUniqueId());
        if (pearlNameFaker != null) {
            pearlNameFaker.cancel();
        }
    }

    /**
     * Runnable to show remaining Enderpearl cooldown on held item.
     */
    public static class PearlNameFaker extends BukkitRunnable {

        private final PlayerTimer timer;
        private final Player player;

        public PearlNameFaker(PlayerTimer timer, Player player) {
            this.timer = timer;
            this.player = player;
        }

        @Override
        public void run() {
            ItemStack stack = player.getItemInHand();
            if (stack != null && stack.getType() == Material.ENDER_PEARL) {
                long remaining = timer.getRemaining(player);
                net.minecraft.server.v1_7_R4.ItemStack item = ((CraftItemStack) stack).handle;
                if (remaining > 0L) {
                    item = item.cloneItemStack();
                    item.c(ChatColor.GOLD + "Enderpearl Cooldown: " + ChatColor.RED + DurationFormatter.getRemaining(remaining, true, true));
                    setFakeItem(item, player.getInventory().getHeldItemSlot());
                } else {
                    cancel();
                }
            }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            setFakeItem(CraftItemStack.asNMSCopy(player.getItemInHand()), player.getInventory().getHeldItemSlot()); // show the original item here.
        }

        /**
         * Sends a fake SetSlot packet to a {@link Player}.
         *
         * @param nms   the {@link net.minecraft.server.v1_7_R4.ItemStack} to set at
         * @param index the inventory index position to set at
         */
        public void setFakeItem(net.minecraft.server.v1_7_R4.ItemStack nms, int index) {
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

            // Taken from CraftInventoryPlayer
            if (index < PlayerInventory.getHotbarSize())
                index = index + 36;
            else if (index > 35)
                index = 8 - (index - 36);

            entityPlayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(entityPlayer.activeContainer.windowId, index, nms));
        }
    }
}
