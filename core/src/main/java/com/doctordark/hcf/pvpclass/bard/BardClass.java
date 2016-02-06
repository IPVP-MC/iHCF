package com.doctordark.hcf.pvpclass.bard;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.hcf.pvpclass.PvpClass;
import com.doctordark.util.BukkitUtils;
import com.doctordark.hcf.util.DurationFormatter;
import com.doctordark.util.chat.Lang;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BardClass extends PvpClass implements Listener {

    public static final int HELD_EFFECT_DURATION_TICKS = 100; // the amount of time in ticks to apply a Held potion effect for faction members

    private static final long BUFF_COOLDOWN_MILLIS = TimeUnit.SECONDS.toMillis(8L); // time in milliseconds for Bard buff cooldowns
    private static final int TEAMMATE_NEARBY_RADIUS = 25;
    private static final long HELD_REAPPLY_TICKS = 20L;

    private final Map<UUID, BardData> bardDataMap = new HashMap<>();
    private final Map<Material, BardEffect> bardEffects = new EnumMap<>(Material.class);

    private final HCF plugin;

    public BardClass(HCF plugin) {
        super("Bard", TimeUnit.SECONDS.toMillis(10L));
        this.plugin = plugin;

        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

        this.bardEffects.put(Material.SUGAR, new BardEffect(45,
                new PotionEffect(PotionEffectType.SPEED, 120, 2),
                new PotionEffect(PotionEffectType.SPEED, HELD_EFFECT_DURATION_TICKS, 1)));
        this.bardEffects.put(Material.BLAZE_POWDER, new BardEffect(50,
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 0),
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, HELD_EFFECT_DURATION_TICKS, 0)));
        this.bardEffects.put(Material.IRON_INGOT, new BardEffect(35,
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 2),
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, HELD_EFFECT_DURATION_TICKS, 0)));
        this.bardEffects.put(Material.GHAST_TEAR, new BardEffect(30,
                new PotionEffect(PotionEffectType.REGENERATION, 60, 2),
                new PotionEffect(PotionEffectType.REGENERATION, HELD_EFFECT_DURATION_TICKS, 0)));
        this.bardEffects.put(Material.FEATHER, new BardEffect(40,
                new PotionEffect(PotionEffectType.JUMP, 120, 2),
                new PotionEffect(PotionEffectType.JUMP, HELD_EFFECT_DURATION_TICKS, 1)));
    }

    @Override
    public boolean onEquip(Player player) {
        if (plugin.getTimerManager().getInvincibilityTimer().getRemaining(player) > 0L) {
            player.sendMessage(ChatColor.RED + "You cannot equip the " + getName() + " PVP Class whilst PVP Protected or eligible for it.");
            return false;
        }

        if (!super.onEquip(player)) {
            return false;
        }

        BardData bardData = new BardData();
        bardDataMap.put(player.getUniqueId(), bardData);
        bardData.startEnergyTracking();
        bardData.heldTask = new BukkitRunnable() {
            int lastEnergy;

            @Override
            public void run() {
                // Apply the bard effects here.
                ItemStack held = player.getItemInHand();
                if (held != null) {
                    BardEffect bardEffect = bardEffects.get(held.getType());
                    if (bardEffect == null) return;

                    if (bardEffect.heldable.getType() == PotionEffectType.JUMP) {
                        plugin.getEffectRestorer().setRestoreEffect(player, bardEffect.heldable);
                    }

                    if (!plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone()) {
                        // Apply the held effect to faction members.
                        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                        if (playerFaction != null) {
                            Collection<Entity> nearbyEntities = player.getNearbyEntities(TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS);
                            for (Entity nearby : nearbyEntities) {
                                if (nearby instanceof Player && !player.equals(nearby)) {
                                    Player target = (Player) nearby;
                                    if (playerFaction.getMembers().containsKey(target.getUniqueId())) {
                                        plugin.getEffectRestorer().setRestoreEffect(target, bardEffect.heldable);
                                    }
                                }
                            }
                        }
                    }
                }

                int energy = (int) getEnergy(player);
                // the -1 check is for offsets with the energy per millisecond
                if (energy != 0 && energy != lastEnergy && (energy % 10 == 0 || lastEnergy - energy - 1 > 0 || energy == BardData.MAX_ENERGY)) {
                    lastEnergy = energy;
                    player.sendMessage(ChatColor.GOLD + name + " energy is now at " + ChatColor.RED + energy + ChatColor.GOLD + '.');
                }
            }
        }.runTaskTimer(plugin, 0L, HELD_REAPPLY_TICKS);
        return true;
    }

    @Override
    public void onUnequip(Player player) {
        super.onUnequip(player);
        clearBardData(player.getUniqueId());
    }

    private void clearBardData(UUID uuid) {
        BardData bardData = bardDataMap.remove(uuid);
        if (bardData != null && bardData.getHeldTask() != null) {
            bardData.getHeldTask().cancel();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearBardData(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        clearBardData(event.getPlayer().getUniqueId());
    }

    private final TObjectLongMap<UUID> msgCooldowns = new TObjectLongHashMap<>();
    private static final String MARK = BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 8);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PvpClass equipped = plugin.getPvpClassManager().getEquippedClass(player);
        if (equipped == null || !equipped.equals(this)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long lastMessage = msgCooldowns.get(uuid);
        long millis = System.currentTimeMillis();
        if (lastMessage != msgCooldowns.getNoEntryValue() && lastMessage - millis > 0L) {
            return;
        }

        ItemStack newStack = player.getInventory().getItem(event.getNewSlot());
        if (newStack != null) {
            BardEffect bardEffect = bardEffects.get(newStack.getType());
            if (bardEffect != null) {
                msgCooldowns.put(uuid, millis + 1500L);
                player.sendMessage(ChatColor.RED + " Bard Effect: " + ChatColor.GOLD + MARK);
                player.sendMessage(ChatColor.YELLOW + "  Clickable Effect: " + ChatColor.AQUA +
                        Lang.fromPotionEffectType(bardEffect.clickable.getType()) + ' ' + (bardEffect.clickable.getAmplifier() + 1) +
                        ChatColor.GRAY + " (" + (bardEffect.clickable.getDuration() / 20) + "s)");
                player.sendMessage(ChatColor.YELLOW + "  Energy Cost: " + ChatColor.AQUA + bardEffect.energyCost);
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || (!event.isCancelled() && action == Action.RIGHT_CLICK_BLOCK)) {
            ItemStack stack = event.getItem();
            BardEffect bardEffect = bardEffects.get(stack.getType());
            if (bardEffect == null || bardEffect.clickable == null) return;

            // Cancel general consuming of this item.
            event.setUseItemInHand(Event.Result.DENY);
            Player player = event.getPlayer();
            BardData bardData = bardDataMap.get(player.getUniqueId());
            if (bardData != null) {
                if (!canUseBardEffect(player, bardData, bardEffect, true)) {
                    return;
                }

                // Safely remove this players item.
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    player.setItemInHand(new ItemStack(Material.AIR, 1));
                }

                if (!plugin.getFactionManager().getFactionAt(player.getLocation()).isSafezone()) {
                    // Apply the held effect to faction members.
                    PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                    if (playerFaction != null) {
                        Collection<Entity> nearbyEntities = player.getNearbyEntities(TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS, TEAMMATE_NEARBY_RADIUS);
                        for (Entity nearby : nearbyEntities) {
                            if (nearby instanceof Player && !player.equals(nearby)) {
                                Player target = (Player) nearby;
                                if (playerFaction.getMembers().containsKey(target.getUniqueId())) {
                                    plugin.getEffectRestorer().setRestoreEffect(target, bardEffect.clickable);
                                }
                            }
                        }
                    }
                }

                plugin.getEffectRestorer().setRestoreEffect(player, bardEffect.clickable);
                bardData.setBuffCooldown(BUFF_COOLDOWN_MILLIS);

                double newEnergy = this.setEnergy(player, bardData.getEnergy() - bardEffect.energyCost);
                player.sendMessage(ChatColor.YELLOW + "You have just used " + name + " buff " + ChatColor.AQUA +
                        Lang.fromPotionEffectType(bardEffect.clickable.getType()) + ' ' + (bardEffect.clickable.getAmplifier() + 1) + ChatColor.YELLOW + " costing you " +
                        ChatColor.BOLD + bardEffect.energyCost + ChatColor.YELLOW + " energy. " +
                        "Your energy is now " + ChatColor.GREEN + ((newEnergy * 10.0) / 10.0)/*TODO:neeeded?*/ + ChatColor.YELLOW + '.');
            }
        }
    }

    private boolean canUseBardEffect(Player player, BardData bardData, BardEffect bardEffect, boolean sendFeedback) {
        String errorFeedback = null;
        double currentEnergy = bardData.getEnergy();
        if (bardEffect.energyCost > currentEnergy) {
            errorFeedback = ChatColor.RED + "You need at least " + ChatColor.BOLD + bardEffect.energyCost +
                    ChatColor.RED + " energy to use this Bard buff, whilst you only have " +
                    ChatColor.BOLD + currentEnergy + ChatColor.RED + '.';
        }

        long remaining = bardData.getRemainingBuffDelay();
        if (remaining > 0L) {
            errorFeedback = ChatColor.RED + "You cannot use this Bard buff for another " + ChatColor.BOLD + DurationFormatter.getRemaining(remaining, true, false) + ChatColor.RED + '.';
        }

        Faction factionAt = plugin.getFactionManager().getFactionAt(player.getLocation());
        if (factionAt.isSafezone()) {
            errorFeedback = ChatColor.RED + "You may not use Bard buffs in safe-zones.";
        }

        if (sendFeedback && errorFeedback != null) {
            player.sendMessage(errorFeedback);
        }

        return errorFeedback == null;
    }

    @Override
    public boolean isApplicableFor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType() != Material.GOLD_HELMET) return false;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() != Material.GOLD_CHESTPLATE) return false;

        ItemStack leggings = player.getInventory().getLeggings();
        if (leggings == null || leggings.getType() != Material.GOLD_LEGGINGS) return false;

        ItemStack boots = player.getInventory().getBoots();
        return !(boots == null || boots.getType() != Material.GOLD_BOOTS);
    }

    public long getRemainingBuffDelay(Player player) {
        synchronized (bardDataMap) {
            BardData bardData = bardDataMap.get(player.getUniqueId());
            return bardData == null ? 0L : bardData.getRemainingBuffDelay();
        }
    }

    /**
     * Gets the energy of a {@link Player}.
     *
     * @param player the {@link Player} to get for
     * @return the energy, or 0 if not tracking this player
     */
    public double getEnergy(Player player) {
        synchronized (bardDataMap) {
            BardData bardData = bardDataMap.get(player.getUniqueId());
            return bardData == null ? 0 : bardData.getEnergy();
        }
    }

    public long getEnergyMillis(Player player) {
        synchronized (bardDataMap) {
            BardData bardData = bardDataMap.get(player.getUniqueId());
            return bardData == null ? 0 : bardData.getEnergyMillis();
        }
    }

    /**
     * Sets the energy of a {@link Player}.
     *
     * @param player the {@link Player} to set for
     * @param energy the energy amount to set
     * @return the new energy amount
     */
    public double setEnergy(Player player, double energy) {
        BardData bardData = bardDataMap.get(player.getUniqueId());
        if (bardData == null) return 0.0;

        bardData.setEnergy(energy);
        return bardData.getEnergy();
    }
}
