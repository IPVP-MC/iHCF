package org.ipvp.hcf.pvpclass.type;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.ipvp.hcf.ConfigurationService;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.pvpclass.PvpClass;

import java.util.concurrent.TimeUnit;

public class RogueClass extends PvpClass implements Listener {

    private final HCF plugin;

    public RogueClass(HCF plugin) {
        super("Rogue", TimeUnit.SECONDS.toMillis(15L));

        this.plugin = plugin;
        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (entity instanceof Player && damager instanceof Player) {
            Player attacker = (Player) damager;
            if (plugin.getPvpClassManager().getEquippedClass(attacker) == this) {
                ItemStack stack = attacker.getItemInHand();
                if (stack != null && stack.getType() == Material.GOLD_SWORD && stack.getEnchantments().isEmpty()) {
                    Player player = (Player) entity;
                    player.sendMessage(ConfigurationService.ENEMY_COLOUR + attacker.getName() + ChatColor.YELLOW + " has backstabbed you.");
                    player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);

                    attacker.sendMessage(ChatColor.YELLOW + "You have backstabbed " + ConfigurationService.ENEMY_COLOUR + player.getName() + ChatColor.YELLOW + '.');
                    attacker.setItemInHand(new ItemStack(Material.AIR, 1));
                    attacker.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);

                    event.setDamage(3.0);
                }
            }
        }
    }

    @Override
    public boolean isApplicableFor(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack helmet = playerInventory.getHelmet();
        if (helmet == null || helmet.getType() != Material.CHAINMAIL_HELMET) {
            return false;
        }

        ItemStack chestplate = playerInventory.getChestplate();
        if (chestplate == null || chestplate.getType() != Material.CHAINMAIL_CHESTPLATE) {
            return false;
        }

        ItemStack leggings = playerInventory.getLeggings();
        if (leggings == null || leggings.getType() != Material.CHAINMAIL_LEGGINGS) {
            return false;
        }

        ItemStack boots = playerInventory.getBoots();
        return !(boots == null || boots.getType() != Material.CHAINMAIL_BOOTS);
    }
}
