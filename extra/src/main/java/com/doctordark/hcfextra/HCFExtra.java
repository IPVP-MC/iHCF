package com.doctordark.hcfextra;

import com.doctordark.hcfextra.command.CoordsCommand;
import com.doctordark.hcfextra.command.HCFExtraCommand;
import com.doctordark.hcfextra.command.HelpCommand;
import com.doctordark.hcfextra.inventoryrestore.InventoryRestoreHandler;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class HCFExtra extends JavaPlugin {

    @Getter
    private static HCFExtra plugin;

    @Getter
    private Configuration configuration;

    @Override
    public void onEnable() {
        HCFExtra.plugin = this;
        PluginCommand temp;

        (this.configuration = new Configuration(this)).reload();

        InventoryRestoreHandler inventoryRestoreHandler = new InventoryRestoreHandler();
        this.getServer().getPluginManager().registerEvents(inventoryRestoreHandler, this);
        (temp = this.getCommand("inv")).setExecutor(inventoryRestoreHandler);
        temp.setPermission("ihcfextra.command.inv");

        (temp = this.getCommand("hcfextra")).setExecutor(new HCFExtraCommand(this));
        temp.setPermission("ihcfextra.command.hcfextra");

        this.getCommand("coords").setExecutor(new CoordsCommand(this));
        this.getCommand("help").setExecutor(new HelpCommand(this));
    }

    @Override
    public void onDisable() {
        HCFExtra.plugin = null;
    }
}
