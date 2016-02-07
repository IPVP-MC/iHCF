package com.doctordark.hcf.util;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PacketPlayOutSetSlot;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class NmsUtils {

    public static int getProtocolVersion(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion();
    }

    public static void resendHeldItemPacket(Player player) {
        sendItemPacketAtHeldSlot(player, NmsUtils.getCleanHeldItem(player));
    }

    public static void sendItemPacketAtHeldSlot(Player player, net.minecraft.server.v1_7_R4.ItemStack stack) {
        sendItemPacketAtSlot(player, stack, player.getInventory().getHeldItemSlot());
    }

    public static void sendItemPacketAtSlot(Player player, net.minecraft.server.v1_7_R4.ItemStack stack, int index) {
        sendItemPacketAtSlot(player, stack, index, ((CraftPlayer) player).getHandle().defaultContainer.windowId);
    }

    public static void sendItemPacketAtSlot(Player player, net.minecraft.server.v1_7_R4.ItemStack stack, int index, int windowID) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        if (entityPlayer.playerConnection != null) {
            // Safeguarding
            if (index < net.minecraft.server.v1_7_R4.PlayerInventory.getHotbarSize()) {
                index += 36;
            } else if (index > 35) {
                index = 8 - (index - 36);
            }

            entityPlayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(windowID, index, stack));
        }
    }

    public static net.minecraft.server.v1_7_R4.ItemStack getCleanItem(Inventory inventory, int slot) {
        return ((CraftInventory) inventory).getInventory().getItem(slot);
    }

    public static net.minecraft.server.v1_7_R4.ItemStack getCleanItem(Player player, int slot) {
        return getCleanItem(player.getInventory(), slot);
    }

    public static net.minecraft.server.v1_7_R4.ItemStack getCleanHeldItem(Player player) {
        return getCleanItem(player, player.getInventory().getHeldItemSlot());
    }

    public static net.minecraft.server.v1_7_R4.ItemStack getDirectNmsItemstack(ItemStack stack) {
        net.minecraft.server.v1_7_R4.ItemStack result = null;
        if (stack != null && stack.getType() != Material.AIR) {
            CraftItemStack craftItemStack = (stack instanceof CraftItemStack ? (CraftItemStack) stack : CraftItemStack.asCraftCopy(stack));
            Object object;
            try {
                Field field = craftItemStack.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                object = field.get(craftItemStack);
                if (object instanceof net.minecraft.server.v1_7_R4.ItemStack) {
                    result = (net.minecraft.server.v1_7_R4.ItemStack) object;
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }

        return result;
    }
}
