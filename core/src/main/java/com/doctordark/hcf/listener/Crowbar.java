package com.doctordark.hcf.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a tool to collect End Frame Portals or Spawners.
 */
public class Crowbar {

    public static final int MAX_SPAWNER_USES = 1;
    public static final int MAX_END_FRAME_USES = 5;
    public static final Material CROWBAR_TYPE = Material.DIAMOND_HOE;
    public static final String CROWBAR_NAME = ChatColor.RED.toString() + "Crowbar";
    public static final String SPAWNER_USE_TAG = "Spawner Uses";
    public static final String END_FRAME_USE_TAG = "End Frame Uses";

    private static final String LORE_FORMAT = ChatColor.GRAY + "%1$s: " + ChatColor.YELLOW + "%2$s/%3$s";

    private int endFrameUses;
    private int spawnerUses;

    private final ItemStack stack;
    private boolean needsMetaUpdate;

    /**
     * Constructs an {@link Crowbar} defaulting to max Spawner and End Frame uses.
     */
    public Crowbar() {
        this(MAX_SPAWNER_USES, MAX_END_FRAME_USES);
    }

    /**
     * Constructs an {@link Crowbar} with given Spawner uses and End Frame uses.
     *
     * @param spawnerUses  the amount of Spawner uses to create with
     * @param endFrameUses the amount of End Frame uses to create with
     */
    public Crowbar(int spawnerUses, int endFrameUses) {
        Objects.requireNonNull(spawnerUses > 0 || endFrameUses > 0, "Cannot create a crowbar with empty uses");
        this.stack = new ItemStack(CROWBAR_TYPE, 1);
        this.setSpawnerUses(Math.min(MAX_SPAWNER_USES, spawnerUses));
        this.setEndFrameUses(Math.min(MAX_END_FRAME_USES, endFrameUses));
    }

    /**
     * Parses an {@link Crowbar} from a given {@link ItemStack}.
     *
     * @param stack the stack to parse from
     * @return the {@link Crowbar} instance
     */
    public static Optional<Crowbar> fromStack(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return Optional.empty();
        }

        ItemMeta meta = stack.getItemMeta();
        if (!meta.hasDisplayName() || !meta.hasLore() || !meta.getDisplayName().equals(CROWBAR_NAME)) {
            return Optional.empty();
        }

        Crowbar crowbar = new Crowbar();
        List<String> loreList = meta.getLore();
        for (String lore : loreList) {
            lore = ChatColor.stripColor(lore);
            int length = lore.length();
            for (int i = 0; i < length; i++) {
                char character = lore.charAt(i);
                if (Character.isDigit(character)) {
                    int amount = Integer.parseInt(String.valueOf(character));
                    if (lore.startsWith(END_FRAME_USE_TAG)) {
                        crowbar.setEndFrameUses(amount);
                        break;
                    } else if (lore.startsWith(SPAWNER_USE_TAG)) {
                        crowbar.setSpawnerUses(amount);
                        break;
                    }
                }
            }
        }

        return Optional.of(crowbar);
    }

    /**
     * Gets the remaining End Frame uses for this {@link Crowbar}.
     *
     * @return the remaining uses
     */
    public int getEndFrameUses() {
        return endFrameUses;
    }

    /**
     * Sets the remaining end frame uses for this {@link Crowbar}.
     *
     * @param uses the uses to set
     */
    public void setEndFrameUses(int uses) {
        if (this.endFrameUses != uses) {
            this.endFrameUses = Math.min(MAX_END_FRAME_USES, uses);
            this.needsMetaUpdate = true;
        }
    }

    /**
     * Gets the remaining Spawner uses for this {@link Crowbar}.
     *
     * @return the remaining uses
     */
    public int getSpawnerUses() {
        return spawnerUses;
    }

    /**
     * Sets the remaining spawner uses for this {@link Crowbar}.
     *
     * @param uses the uses to set
     */
    public void setSpawnerUses(int uses) {
        if (this.spawnerUses != uses) {
            this.spawnerUses = Math.min(MAX_SPAWNER_USES, uses);
            this.needsMetaUpdate = true;
        }
    }

    /**
     * Converts this {@link Crowbar} to a {@link ItemStack}.
     *
     * @return the converted {@link ItemStack} or an {@link Material#AIR} if fully used
     */
    public ItemStack getItemIfPresent() {
        Optional<ItemStack> optional = toItemStack();
        return optional.isPresent() ? optional.get() : new ItemStack(Material.AIR, 1);
    }

    /**
     * Converts this {@link Crowbar} to an optional {@link ItemStack}.
     *
     * @return the converted {@link ItemStack} or an {@link Optional#empty()} if fully used
     */
    public Optional<ItemStack> toItemStack() {
        if (needsMetaUpdate) {
            double maxDurability = CROWBAR_TYPE.getMaxDurability();
            double curDurability = maxDurability;
            double increment = curDurability / ((double) Crowbar.MAX_SPAWNER_USES + Crowbar.MAX_END_FRAME_USES);
            curDurability -= increment * ((double) spawnerUses + endFrameUses);
            if (Math.abs(curDurability - maxDurability) == 0) {
                return Optional.empty();
            }

            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(CROWBAR_NAME);
            meta.setLore(Arrays.asList(
                    String.format(LORE_FORMAT, SPAWNER_USE_TAG, spawnerUses, MAX_SPAWNER_USES),
                    String.format(LORE_FORMAT, END_FRAME_USE_TAG, endFrameUses, MAX_END_FRAME_USES))
            );

            stack.setItemMeta(meta);
            stack.setDurability((short) curDurability);
            needsMetaUpdate = false;
        }

        return Optional.of(stack);
    }
}