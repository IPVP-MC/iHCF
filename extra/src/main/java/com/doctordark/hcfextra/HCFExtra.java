package com.doctordark.hcfextra;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import com.doctordark.hcfextra.command.CoordsCommand;
import com.doctordark.hcfextra.command.HCFExtraCommand;
import com.doctordark.hcfextra.command.HelpCommand;

public class HCFExtra extends JavaPlugin {

    @Getter
    private static HCFExtra plugin;

    @Getter
    private Configuration configuration;

    @Override
    public void onEnable() {
        HCFExtra.plugin = this;

        (this.configuration = new Configuration(this)).reload();

        this.getCommand("coords").setExecutor(new CoordsCommand(this));
        this.getCommand("hcfextra").setExecutor(new HCFExtraCommand(this));
        this.getCommand("help").setExecutor(new HelpCommand(this));
    }

    @Override
    public void onDisable() {
        HCFExtra.plugin = null;
    }
}
