package com.doctordark.hcf.economy;

import com.doctordark.util.Config;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the {@link FlatFileEconomyManager} storing to YAML.
 */
public class FlatFileEconomyManager implements EconomyManager {

    private final JavaPlugin plugin;

    private TObjectIntMap<UUID> balanceMap = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);
    private Config balanceConfig;

    public FlatFileEconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadEconomyData();
    }

    @Override
    public TObjectIntMap<UUID> getBalanceMap() {
        return balanceMap;
    }

    @Override
    public int getBalance(UUID uuid) {
        return balanceMap.get(uuid);
    }

    @Override
    public int setBalance(UUID uuid, int amount) {
        balanceMap.put(uuid, amount);
        return amount;
    }

    @Override
    public int addBalance(UUID uuid, int amount) {
        return setBalance(uuid, getBalance(uuid) + amount);
    }

    @Override
    public int subtractBalance(UUID uuid, int amount) {
        return setBalance(uuid, getBalance(uuid) - amount);
    }

    @Override
    public void reloadEconomyData() {
        balanceConfig = new Config(plugin, "balances");
        Object object = balanceConfig.get("balances");
        if (object instanceof MemorySection) {
            MemorySection section = (MemorySection) object;
            Set<String> keys = section.getKeys(false);
            for (String id : keys) {
                balanceMap.put(UUID.fromString(id), balanceConfig.getInt("balances." + id));
            }
        }
    }

    @Override
    public void saveEconomyData() {
        Map<String, Integer> saveMap = new LinkedHashMap<>(balanceMap.size());
        balanceMap.forEachEntry(new TObjectIntProcedure<UUID>() {
            @Override
            public boolean execute(UUID uuid, int i) {
                saveMap.put(uuid.toString(), i);
                return true;
            }
        });

        balanceConfig.set("balances", saveMap);
        balanceConfig.save();
    }
}
