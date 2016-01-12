package com.doctordark.hcf.eventgame.crate;

import com.doctordark.hcf.HCF;
import com.doctordark.util.Config;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manager used to handle all of the crate {@link Key}s.
 */
public class KeyManager {

    private final EventKey eventKey;

    private final Table<UUID, String, Integer> depositedCrateMap = HashBasedTable.create();
    private final Set<Key> keys;
    private final Config config;

    public KeyManager(HCF plugin) {
        eventKey = new EventKey();

        this.config = new Config(plugin, "key-data");
        this.keys = Sets.newHashSet(eventKey);
        reloadKeyData();
    }

    /**
     * Gets the map for crate key deposits of a player.
     *
     * @param uuid the uuid of player to get for
     * @return map with key as key name and value as amount
     */
    public Map<String, Integer> getDepositedCrateMap(UUID uuid) {
        return depositedCrateMap.row(uuid);
    }

    /**
     * Gets the set of {@link Key}s held
     * by this manager.
     *
     * @return set of {@link Key}s
     */
    public Set<Key> getKeys() {
        return keys;
    }

    public EventKey getEventKey() {
        return eventKey;
    }

    /**
     * Gets a {@link Key} by its name.
     *
     * @param name the name to search for
     * @return the {@link Key} with the name
     */
    public Key getKey(String name) {
        for (Key key : keys) {
            if (key.getName().equalsIgnoreCase(name)) {
                return key;
            }
        }

        return null;
    }

    /**
     * Gets a {@link Key} from a given class.
     *
     * @param clazz the class to get from
     * @return the {@link Key} that is assignable from class
     */
    @Deprecated
    public Key getKey(Class<? extends Key> clazz) {
        for (Key key : keys) {
            if (clazz.isAssignableFrom(key.getClass())) {
                return key;
            }
        }

        return null;
    }

    /**
     * Gets a {@link Key} from an {@link ItemStack}.
     *
     * @param stack the {@link ItemStack} to get from
     * @return the {@link Key}, or null if is not a {@link Key}
     */
    public Key getKey(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return null;
        }

        for (Key key : keys) {
            ItemStack item = key.getItemStack();
            if (item.getItemMeta().getDisplayName().equals(stack.getItemMeta().getDisplayName())) {
                return key;
            }
        }

        return null;
    }

    /**
     * Loads the {@link Key} data from storage.
     */
    public void reloadKeyData() {
        for (Key key : keys) {
            key.load(config);
        }

        Object object = config.get("deposited-key-map");
        if (object instanceof MemorySection) {
            MemorySection section = (MemorySection) object;
            for (String id : section.getKeys(false)) {
                object = config.get(section.getCurrentPath() + '.' + id);
                if (object instanceof MemorySection) {
                    section = (MemorySection) object;
                    for (String key : section.getKeys(false)) {
                        depositedCrateMap.put(UUID.fromString(id), key, config.getInt("deposited-key-map." + id + '.' + key));
                    }
                }
            }
        }
    }

    /**
     * Saves the {@link Key} data to storage.
     */
    public void saveKeyData() {
        for (Key key : keys) {
            key.save(config);
        }

        Map<String, Map<String, Integer>> saveMap = new LinkedHashMap<>(depositedCrateMap.size());
        for (Map.Entry<UUID, Map<String, Integer>> entry : depositedCrateMap.rowMap().entrySet()) {
            saveMap.put(entry.getKey().toString(), entry.getValue());
        }

        config.set("deposited-key-map", saveMap);
        config.save();
    }
}