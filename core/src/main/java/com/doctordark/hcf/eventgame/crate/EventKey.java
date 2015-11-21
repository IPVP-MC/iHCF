package com.doctordark.hcf.eventgame.crate;

import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.util.Config;
import com.doctordark.util.InventorySerialisation;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EventKey extends Key {

    private final ArrayListMultimap<String, Inventory> inventories = ArrayListMultimap.create();

    public EventKey() {
        super("Event");
    }

    /**
     * Gets the possible {@link Inventory}s of this {@link EventKey} for a given {@link EventType}.
     *
     * @param eventType the {@link EventType} to get for
     * @return list of {@link EventKey} inventories
     */
    public List<Inventory> getInventories(EventType eventType) {
        return inventories.get(eventType.name());
    }

    public EventKeyData getData(List<String> itemLore) {
        if (itemLore.size() < 2) {
            return null;
        }

        String first = ChatColor.stripColor(itemLore.get(1));
        if (first == null) return null;

        for (EventType eventType : EventType.values()) {
            if (first.contains(eventType.getDisplayName())) {
                return new EventKeyData(eventType, Character.getNumericValue(first.charAt(first.length() - 1)));
            }
        }

        return null;
    }

    @Override
    public ChatColor getColour() {
        return ChatColor.GOLD;
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(getColour() + getName() + " Key");
        meta.setLore(Lists.newArrayList(ChatColor.GRAY + "Right click an empty Chest to use."));
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Converts this {@link EventKey} to an {@link ItemStack} with
     * its  properties.
     *
     * @param eventKeyData the data to base the item around
     * @return the converted {@link ItemStack}
     */
    public ItemStack getItemStack(EventKeyData eventKeyData) {
        ItemStack stack = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(getColour() + getName() + " Key");
        meta.setLore(Lists.newArrayList(
                ChatColor.GRAY + "Right click an empty Chest to use.",
                ChatColor.GRAY + eventKeyData.getEventType().getDisplayName() + ChatColor.YELLOW + " Inventory " + ChatColor.GOLD + eventKeyData.inventoryNumber));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void load(Config config) {
        super.load(config);

        Object object = config.get("event-key-loot");
        if (object instanceof MemorySection) {
            MemorySection section = (MemorySection) object;
            for (String key : section.getKeys(false)) {
                try {
                    Object value = config.get(section.getCurrentPath() + '.' + key);
                    if (value instanceof List<?>) {
                        List<?> list = (List<?>) value;
                        for (Object each : list) {
                            if (each instanceof String) {
                                inventories.put(key, InventorySerialisation.fromBase64((String) each));
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void save(Config config) {
        super.save(config);

        Set<Map.Entry<String, Collection<Inventory>>> entrySet = inventories.asMap().entrySet();
        Map<String, List<String>> flushedInventories = new LinkedHashMap<>(entrySet.size());
        for (Map.Entry<String, Collection<Inventory>> entry : entrySet) {
            flushedInventories.put(entry.getKey(), new ArrayList<>(entry.getValue()).stream().map(InventorySerialisation::toBase64).collect(Collectors.toList()));
        }

        config.set("event-key-loot", flushedInventories);
        config.save();
    }

    /**
     * Data used to store on {@link EventKey}s.
     */
    public static class EventKeyData {

        @Getter
        private final EventType eventType;

        @Getter
        private final int inventoryNumber;

        public EventKeyData(EventType eventType, int inventoryNumber) {
            this.eventType = eventType;
            this.inventoryNumber = inventoryNumber;
        }
    }
}