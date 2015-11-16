package com.doctordark.hcf.combatlog.type;

import com.doctordark.hcf.ConfigurationService;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.combatlog.event.LoggerRemovedEvent;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.PlayerList;
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

import java.util.UUID;

/**
 * Represents a {@link Player} that has combat-logged.
 */
public class LoggerEntityHuman extends EntityPlayer implements LoggerEntity {

    protected BukkitTask removalTask;
    protected final UUID playerUUID;

    public LoggerEntityHuman(Player player, World world) {
        this(player, ((CraftWorld) world).getHandle());
    }

    private LoggerEntityHuman(Player player, WorldServer world) {
        super(MinecraftServer.getServer(), world, new GameProfile(player.getUniqueId(), player.getName()), new PlayerInteractManager(world));

        // Assign variables first.
        final PlayerList playerlist = MinecraftServer.getServer().getPlayerList();
        Location location = player.getLocation();
        double x = location.getX(), y = location.getY(), z = location.getZ();
        float yaw = location.getYaw(), pitch = location.getPitch();

        // Next set the values
        new FakePlayerConnection(this); // also assigns to the EntityPlayer
        this.spawnIn(world);
        this.playerConnection.a(x, y, z, yaw, pitch);
        this.playerUUID = player.getUniqueId();
        this.lastDamager = ((CraftPlayer) player).getHandle().lastDamager;

        /*playerlist.players.add(this);
        playerlist.playersByName.put(this.getName(), this);
        playerlist.uuidMap.put(this.playerUUID, this);*/

        world.addEntity(this);
        Bukkit.getConsoleSender().sendMessage(String.format(ChatColor.GOLD + "Combat logger of " + player.getName() + " has spawned at %.2f, %.2f, %.2f", x, y, z));

        final LoggerEntityHuman finalLogger = this;
        this.removalTask = new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutPlayerInfo packet = PacketPlayOutPlayerInfo.removePlayer(finalLogger.getBukkitEntity().getHandle());
                playerlist.sendAll(packet);
                finalLogger.destroy();
            }
        }.runTaskLater(HCF.getPlugin(), ConfigurationService.COMBAT_LOG_DESPAWN_TICKS);
    }

    private static class FakePlayerConnection extends PlayerConnection {

        public FakePlayerConnection(EntityPlayer entityplayer) {
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

        public FakeNetworkManager() {
            super(false);
        }

        @Override
        public int getVersion() {
            return super.getVersion();
        }
    }

    @Override
    public void die() {
        super.die();

        LoggerRemovedEvent event = new LoggerRemovedEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (this.removalTask != null) {
            this.removalTask.cancel();
            this.removalTask = null;
        }

        /*PlayerList playerlist = MinecraftServer.getServer().getPlayerList();
        playerlist.players.remove(this);
        playerlist.playersByName.remove(this.getName());
        playerlist.uuidMap.remove(this.playerUUID);*/
    }

    @Override
    public void destroy() {
        if (!this.dead) {
            this.die();
        }
    }

    @Override
    public UUID getPlayerUUID() {
        return this.playerUUID;
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
