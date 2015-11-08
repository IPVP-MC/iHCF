package com.doctordark.hcf.faction.type;

import com.doctordark.hcf.ConfigurationService;
import org.bukkit.command.CommandSender;

import java.util.Map;

/**
 * Represents the {@link WarzoneFaction}.
 */
public class WarzoneFaction extends Faction {

    public WarzoneFaction() {
        super("Warzone");
    }

    public WarzoneFaction(Map<String, Object> map) {
        super(map);
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        return ConfigurationService.WARZONE_COLOUR + getName();
    }
}
