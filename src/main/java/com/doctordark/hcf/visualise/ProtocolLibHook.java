package com.doctordark.hcf.visualise;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.doctordark.hcf.HCF;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Reference http://wiki.vg/Protocol
 */
public final class ProtocolLibHook {

    private static final int STARTED_DIGGING = 0;
    private static final int FINISHED_DIGGING = 2;

    private ProtocolLibHook() {
    }

    /**
     * Hooks ProtocolLibrary into a {@link JavaPlugin}.
     *
     * @param hcf the plugin to hook into
     */
    public static void hook(HCF hcf) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(hcf,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                StructureModifier<Integer> modifier = packet.getIntegers();
                Player player = event.getPlayer();

                try {
                    int status = modifier.read(4);
                    //int face = modifier.read(3);
                    if (status == STARTED_DIGGING || status == FINISHED_DIGGING) {
                        int x, y, z;
                        Location location = new Location(player.getWorld(), x = modifier.read(0), y = modifier.read(1), z = modifier.read(2));

                        // Validation
                        VisualBlock visualBlock = hcf.getVisualiseHandler().getVisualBlockAt(player, location);
                        if (visualBlock == null) return;

                        event.setCancelled(true);
                        VisualBlockData data = visualBlock.getBlockData();
                        if (status == FINISHED_DIGGING) {
                            player.sendBlockChange(location, data.getBlockType(), data.getData());
                        } else if (status == STARTED_DIGGING) { // we check this because Blocks that broke pretty much straight away do not send a FINISHED for some weird reason.
                            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                            if (player.getGameMode() == GameMode.CREATIVE ||
                                    net.minecraft.server.v1_7_R4.Block.getById(data.getItemTypeId()).getDamage(entityPlayer, entityPlayer.world, x, y, z) > 1.0F) {

                                player.sendBlockChange(location, data.getBlockType(), data.getData());
                            }
                        }
                    }
                } catch (FieldAccessException ignored) {
                }
            }
        });
    }

    private static boolean isLiquidSource(Material material) {
        switch (material) {
            case LAVA_BUCKET:
            case WATER_BUCKET:
                return true;
            default:
                return false;
        }
    }
}