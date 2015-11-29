package com.doctordark.hcf;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.server.v1_7_R4.ChunkCoordIntPair;
import net.minecraft.server.v1_7_R4.PacketPlayOutMultiBlockChange;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.spigotmc.SpigotDebreakifier;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class VisualiseUtil {

    public static void handleBlockChanges(Player player, Map<Location, MaterialData> input) throws IOException {
        if (input.isEmpty()) {
            return;
        }

        if (input.size() == 1) {
            Map.Entry<Location, MaterialData> entry = input.entrySet().iterator().next();
            MaterialData materialData = entry.getValue();
            player.sendBlockChange(entry.getKey(), materialData.getItemType(), materialData.getData());
            return;
        }

        Table<Chunk, Location, MaterialData> table = HashBasedTable.create();
        for (Map.Entry<Location, MaterialData> entry : input.entrySet()) {
            Location location = entry.getKey();
            if (location.getWorld().isChunkLoaded(((int) location.getX()) >> 4, ((int) location.getZ()) >> 4)) {
                table.row(entry.getKey().getChunk()).put(location, entry.getValue());
            }
        }

        for (Map.Entry<Chunk, Map<Location, MaterialData>> entry : table.rowMap().entrySet()) {
            VisualiseUtil.sendBulk(player, entry.getKey(), entry.getValue());
        }
    }

    private static void sendBulk(Player player, org.bukkit.Chunk chunk, Map<Location, MaterialData> input) throws IOException {
        Preconditions.checkNotNull(chunk, "Chunk cannot be null");

        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
        packet.chunk = ((CraftChunk) chunk).getHandle();

        ChunkCoordIntPair intPair = new ChunkCoordIntPair(chunk.getX(), chunk.getZ());
        int numberOfRecords = input.size();

        packet.b = intPair;
        packet.d = numberOfRecords;

        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(input.size());
        DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);

        packet.ashort = new short[input.size()];
        packet.blocks = new int[input.size()];

        int i = 0;
        for (Map.Entry<Location, MaterialData> entry : input.entrySet()) {
            Location location = entry.getKey();

            int blockID = entry.getValue().getItemTypeId();
            int data = entry.getValue().getData();
            data = SpigotDebreakifier.getCorrectedData(blockID, data);

            packet.blocks[i] = (blockID & 4095) << 4 | data & 15;
            packet.ashort[i] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());

            dataoutputstream.writeShort(packet.ashort[i]);
            dataoutputstream.writeShort(packet.blocks[i]);
            i++;
        }

        int expectedSize = input.size() * 4;
        byte[] bulk = bytearrayoutputstream.toByteArray();
        Preconditions.checkArgument(bulk.length == expectedSize, "Expected length: '" + expectedSize + "' doesn't match the generated length: '" + bulk.length + "'");

        packet.c = bulk;
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
