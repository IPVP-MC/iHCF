package com.doctordark.hcf.combatlog;

import com.google.common.base.Function;
import net.minecraft.server.v1_7_R4.DamageSource;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EntitySkeleton;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a {@link Player} that has combat-logged.
 */
public class LoggerEntity extends EntitySkeleton {

    private final UUID playerUUID;

    public LoggerEntity(World world, Location location, Player player) {
        super(((CraftWorld) world).getHandle());

        this.goalSelector.b.clear();
        this.goalSelector.c.clear();

        this.lastDamager = ((CraftPlayer) player).getHandle().lastDamager;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        String playerName = player.getName();
        boolean hasSpawned = ((CraftWorld) world).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Combat Logger for [" + playerName + "] " +
                (hasSpawned ? ChatColor.GREEN + "successfully spawned" : ChatColor.RED + "failed to spawn") + ChatColor.GOLD + " at (" +
                String.format("%.1f", x) + ", " + String.format("%.1f", y) + ", " + String.format("%.1f", z) + ')');

        this.playerUUID = player.getUniqueId(); // Allocate objects if we know it spawned.
        if (hasSpawned) {
            this.setCustomName(playerName);
            this.setCustomNameVisible(true);
            this.setPositionRotation(x, y, z, location.getYaw(), location.getPitch());
        }
    }

    /**
     * Gets the {@link UUID} of the represented.
     *
     * @return the represented {@link UUID}
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override // prevents the entity from moving
    public void move(double d0, double d1, double d2) {
    }

    @Override // prevents the entity going through portals
    public void b(int i) {
    }

    @Override // prevents the entity dropping loot
    public void dropDeathLoot(boolean flag, int i) {
    }

    @Override // prevents the entity finding a target to kill
    public Entity findTarget() {
        return null;
    }

    private static final Function<Double, Double> DAMAGE_FUNCTION = f1 -> 0.0D;

    @Override // calls the event so we can prevent players attacking if same faction, etc
    public boolean damageEntity(DamageSource damageSource, float amount) {
        PlayerNmsResult nmsResult = getResult(world.getWorld(), playerUUID);
        if (nmsResult == null) return true;

        EntityPlayer entityPlayer = nmsResult.entityPlayer;
        if (entityPlayer != null) {
            entityPlayer.setPosition(locX, locY, locZ);
            EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent(
                    entityPlayer, damageSource, amount, 0D, 0D, 0D, 0D, 0D, 0D,
                    DAMAGE_FUNCTION, DAMAGE_FUNCTION, DAMAGE_FUNCTION, DAMAGE_FUNCTION, DAMAGE_FUNCTION, DAMAGE_FUNCTION);

            // If the event says it should be, it should be.
            if (event.isCancelled()) return false;
        }

        // Otherwise, return what the super says.
        return super.damageEntity(damageSource, amount);
    }

    @Override // human interaction
    public boolean a(EntityHuman entityHuman) {
        return false;
    }

    @Override // movement
    public void h() {
        super.h();
    }

    @Override // collision
    public void collide(Entity entity) {
    }

    @Override
    public void die(DamageSource damageSource) {
        PlayerNmsResult playerNmsResult = getResult(world.getWorld(), playerUUID);
        if (playerNmsResult == null) return;

        Player player = playerNmsResult.player;
        PlayerInventory inventory = player.getInventory();
        boolean keepInventory = world.getGameRules().getBoolean("keepInventory");
        List<ItemStack> drops = new ArrayList<>();
        if (!keepInventory) {
            for (ItemStack item : inventory.getContents()) {
                if (item == null || item.getType() == Material.AIR) continue;
                drops.add(item);
            }

            for (ItemStack armour : inventory.getArmorContents()) {
                if (armour == null || armour.getType() == Material.AIR) continue;
                drops.add(armour);
            }
        }

        String deathMessage = ChatColor.GOLD + "(" + ChatColor.WHITE + "CombatLogger" + ChatColor.GOLD + ") " + combatTracker.b().c();
        EntityPlayer entityPlayer = playerNmsResult.entityPlayer;
        entityPlayer.combatTracker = combatTracker; // hacky fix to let the called event know who the murderer is ;)

        org.bukkit.event.entity.PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(entityPlayer, drops, deathMessage, keepInventory);
        deathMessage = event.getDeathMessage();
        if (deathMessage != null && !deathMessage.isEmpty()) {
            Bukkit.broadcastMessage(deathMessage);
        }

        // This method resets the combat tracker, so handle the death-message first.
        super.die(damageSource);

        LoggerDeathEvent loggerDeathEvent = new LoggerDeathEvent(this);
        Bukkit.getPluginManager().callEvent(loggerDeathEvent);

        // Clear the inventory if it should.
        if (!event.getKeepInventory()) {
            inventory.clear();
            inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
        }

        // Make sure to correct the players location afterwards.
        entityPlayer.setLocation(locX, locY, locZ, yaw, pitch);
        entityPlayer.setHealth(0.0F);
        player.saveData();
    }


    private static PlayerNmsResult getResult(World world, UUID playerUUID) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        if (offlinePlayer.hasPlayedBefore()) {
            WorldServer worldServer = ((CraftWorld) world).getHandle();
            EntityPlayer entityPlayer = new EntityPlayer(
                    ((CraftServer) Bukkit.getServer()).getServer(), worldServer,
                    new GameProfile(playerUUID, offlinePlayer.getName()),
                    new PlayerInteractManager(worldServer));

            Player player = entityPlayer.getBukkitEntity();
            if (player != null) {
                player.loadData();
                return new PlayerNmsResult(player, entityPlayer);
            }
        }

        return null;
    }

    public static final class PlayerNmsResult {

        public final Player player;
        public final EntityPlayer entityPlayer;

        public PlayerNmsResult(Player player, EntityPlayer entityPlayer) {
            this.player = player;
            this.entityPlayer = entityPlayer;
        }
    }

    @Override
    public CraftLivingEntity getBukkitEntity() {
        return (CraftLivingEntity) super.getBukkitEntity();
    }
}
