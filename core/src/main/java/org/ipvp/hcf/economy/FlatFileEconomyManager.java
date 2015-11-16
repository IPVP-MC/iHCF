package org.ipvp.hcf.economy;

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
        this.reloadEconomyData();
    }

    @Override
    public TObjectIntMap<UUID> getBalanceMap() {
        return this.balanceMap;
    }

    @Override
    public int getBalance(UUID uuid) {
        return this.balanceMap.get(uuid);
    }

    @Override
    public int setBalance(UUID uuid, int amount) {
        this.balanceMap.put(uuid, amount);
        return amount;
    }

    @Override
    public int addBalance(UUID uuid, int amount) {
        return this.setBalance(uuid, this.getBalance(uuid) + amount);
    }

    @Override
    public int subtractBalance(UUID uuid, int amount) {
        return this.setBalance(uuid, this.getBalance(uuid) - amount);
    }

    @Override
    public void reloadEconomyData() {
        Object object = (this.balanceConfig = new Config(plugin, "balances")).get("balances");
        if (object instanceof MemorySection) {
            MemorySection section = (MemorySection) object;
            Set<String> keys = section.getKeys(false);
            for (String id : keys) {
                this.balanceMap.put(UUID.fromString(id), this.balanceConfig.getInt("balances." + id));
            }
        }
    }

    @Override
    public void saveEconomyData() {
        Map<String, Integer> saveMap = new LinkedHashMap<>(this.balanceMap.size());
        this.balanceMap.forEachEntry(new TObjectIntProcedure<UUID>() {
            @Override
            public boolean execute(UUID uuid, int i) {
                saveMap.put(uuid.toString(), i);
                return true;
            }
        });

        this.balanceConfig.set("balances", saveMap);
        this.balanceConfig.save();
    }
}
