package com.doctordark.hcf.combatlog.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.combatlog.event.LoggerDeathEvent;
import com.doctordark.hcf.combatlog.event.LoggerRemovedEvent;
import net.minecraft.server.v1_7_R4.DamageSource;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Represents a {@link Player} that has combat-logged.
 */
public class LoggerEntityHuman extends EntityPlayer implements LoggerEntity {

    private BukkitTask removalTask;

    public LoggerEntityHuman(Player player, World world) {
        this(player, ((CraftWorld) world).getHandle());
    }

    private LoggerEntityHuman(Player player, WorldServer world) {
        super(MinecraftServer.getServer(), world, new GameProfile(player.getUniqueId(), player.getName()), new PlayerInteractManager(world));

        // Assign variables first.
        Location location = player.getLocation();
        double x = location.getX(), y = location.getY(), z = location.getZ();
        float yaw = location.getYaw(), pitch = location.getPitch();

        new FakePlayerConnection(this); // also assigns to the EntityPlayer#playerConnection field.
        playerConnection.a(x, y, z, yaw, pitch); // must instantiate FakePlayerConnection for this to work

        EntityPlayer originPlayer = ((CraftPlayer) player).getHandle();
        lastDamager = originPlayer.lastDamager;
        invulnerableTicks = originPlayer.invulnerableTicks;
        combatTracker = originPlayer.combatTracker;
    }

    @Override
    protected boolean d(final DamageSource source, float amount) {
        if (!dead) {
            boolean flag = super.d(source, amount);
            if (!flag || dead || getHealth() <= 0.0F) { // super call already killed player
                return flag;
            }

            super.die(source);
            dead = true;
            setHealth(0.0F);
            MinecraftServer.getServer().getPlayerList().playerFileData.save(this);
            return true;
        }

        return false;
    }

    public void postSpawn(HCF plugin) {
        if (world.addEntity(this)) {
            Bukkit.getConsoleSender().sendMessage(String.format(ChatColor.GOLD + "Combat logger of " + getName() + " has spawned at %.2f, %.2f, %.2f", locX, locY, locZ));
            MinecraftServer.getServer().getPlayerList().playerFileData.load(this); // load the updated NBT
        } else {
            Bukkit.getConsoleSender().sendMessage(String.format(ChatColor.RED + "Combat logger of " + getName() + " failed to spawned at %.2f, %.2f, %.2f", locX, locY, locZ));
        }

        final LoggerEntityHuman finalLogger = this;
        removalTask = new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutPlayerInfo packet = PacketPlayOutPlayerInfo.removePlayer(
                        finalLogger.getBukkitEntity().getHandle());
                MinecraftServer.getServer().getPlayerList().sendAll(packet);
                finalLogger.destroy();
            }
        }.runTaskLater(plugin, plugin.getConfiguration().getCombatlogDespawnDelayTicks());
    }

    private static class FakePlayerConnection extends PlayerConnection {

        private FakePlayerConnection(EntityPlayer entityplayer) {
            super(MinecraftServer.getServer(), new FakeNetworkManager(), entityplayer);
        }

        @Override
        public void disconnect(String s) {
        }

        @Override
        public void sendPacket(Packet packet) {
        }
    }

    private static class FakeNetworkManager extends NetworkManager {

        private FakeNetworkManager() {
            super(false);
        }

        @Override
        public int getVersion() {
            return super.getVersion();
        }
    }

    @Override
    public void closeInventory() {
    }

    private void cancelTask() {
        if (removalTask != null) {
            removalTask.cancel();
            removalTask = null;
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!dead) {
            super.die(damageSource);
            Bukkit.getPluginManager().callEvent(new LoggerDeathEvent(this));
            MinecraftServer.getServer().getPlayerList().playerFileData.save(this); // Save inventory NBT
            cancelTask();
        }
    }

    @Override
    public void destroy() {
        if (!dead) {
            cancelTask();
            Bukkit.getPluginManager().callEvent(new LoggerRemovedEvent(this));
            dead = true;
        }
    }

    @Override // prevents the entity going through portals
    public void b(int i) {
    }

    @Override // prevents the entity dropping loot
    public void dropDeathLoot(boolean flag, int i) {
    }

    @Override // human interaction
    public boolean a(EntityHuman entityHuman) {
        return super.a(entityHuman);
    }

    @Override // collision
    public void collide(Entity entity) {
    }
}
