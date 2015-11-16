package org.ipvp.hcf.pvpclass.type;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.pvpclass.PvpClass;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AssassinClass extends PvpClass implements Listener {

    private final TObjectLongMap<UUID> cooldowns = new TObjectLongHashMap<>();

    private final HCF plugin;

    public AssassinClass(HCF plugin) {
        super("Assassin", TimeUnit.SECONDS.toMillis(10L));

        this.plugin = plugin;
        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getItem().getType() == Material.GOLDEN_CARROT) {
            Player player = event.getPlayer();
            if (plugin.getPvpClassManager().getEquippedClass(player) == this) {
                long timestamp = cooldowns.get(player.getUniqueId());
                long millis = System.currentTimeMillis();
                long remaining = timestamp == cooldowns.getNoEntryValue() ? 0L : timestamp - millis;
                if (remaining > 0L) {
                    player.sendMessage(ChatColor.RED + "Cooldown still for " + DurationFormatUtils.formatDurationWords(remaining, true, true) + ".");
                    return;
                }

                cooldowns.put(player.getUniqueId(), millis + 15000L);
                plugin.getEffectRestorer().setRestoreEffect(player, new PotionEffect(PotionEffectType.SPEED, 100, 4));
                plugin.getEffectRestorer().setRestoreEffect(player, new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0));
            }
        }
    }

    @Override
    public boolean isApplicableFor(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack helmet = playerInventory.getHelmet();
        if (helmet == null || helmet.getType() != Material.IRON_HELMET || !helmet.getEnchantments().isEmpty())
            return false;

        ItemStack chestplate = playerInventory.getChestplate();
        if (chestplate == null || chestplate.getType() != Material.IRON_CHESTPLATE || !chestplate.getEnchantments().isEmpty())
            return false;

        ItemStack leggings = playerInventory.getLeggings();
        if (leggings == null || leggings.getType() != Material.IRON_LEGGINGS || !leggings.getEnchantments().isEmpty())
            return false;

        ItemStack boots = playerInventory.getBoots();
        return !(boots == null || boots.getType() != Material.IRON_BOOTS || !boots.getEnchantments().isEmpty());
    }
}
