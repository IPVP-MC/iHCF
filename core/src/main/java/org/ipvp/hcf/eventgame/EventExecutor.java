package org.ipvp.hcf.eventgame;

import com.doctordark.util.command.ArgumentExecutor;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.eventgame.argument.EventAddLootTableArgument;
import org.ipvp.hcf.eventgame.argument.EventCancelArgument;
import org.ipvp.hcf.eventgame.argument.EventCreateArgument;
import org.ipvp.hcf.eventgame.argument.EventDelLootTableArgument;
import org.ipvp.hcf.eventgame.argument.EventDeleteArgument;
import org.ipvp.hcf.eventgame.argument.EventRenameArgument;
import org.ipvp.hcf.eventgame.argument.EventSetAreaArgument;
import org.ipvp.hcf.eventgame.argument.EventSetCapzoneArgument;
import org.ipvp.hcf.eventgame.argument.EventSetLootArgument;
import org.ipvp.hcf.eventgame.argument.EventStartArgument;
import org.ipvp.hcf.eventgame.argument.EventUptimeArgument;

/**
 * Handles the execution and tab completion of the event command.
 */
public class EventExecutor extends ArgumentExecutor {

    public EventExecutor(HCF plugin) {
        super("event");

        addArgument(new EventCancelArgument(plugin));
        addArgument(new EventCreateArgument(plugin));
        addArgument(new EventDeleteArgument(plugin));
        addArgument(new EventRenameArgument(plugin));
        addArgument(new EventSetAreaArgument(plugin));
        addArgument(new EventSetCapzoneArgument(plugin));
        addArgument(new EventAddLootTableArgument(plugin));
        addArgument(new EventDelLootTableArgument(plugin));
        addArgument(new EventSetLootArgument(plugin));
        addArgument(new EventStartArgument(plugin));
        addArgument(new EventUptimeArgument(plugin));
    }
}