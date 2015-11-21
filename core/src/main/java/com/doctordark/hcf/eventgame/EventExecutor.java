package com.doctordark.hcf.eventgame;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.argument.EventAddLootTableArgument;
import com.doctordark.hcf.eventgame.argument.EventCancelArgument;
import com.doctordark.hcf.eventgame.argument.EventCreateArgument;
import com.doctordark.hcf.eventgame.argument.EventDelLootTableArgument;
import com.doctordark.hcf.eventgame.argument.EventDeleteArgument;
import com.doctordark.hcf.eventgame.argument.EventRenameArgument;
import com.doctordark.hcf.eventgame.argument.EventSetAreaArgument;
import com.doctordark.hcf.eventgame.argument.EventSetCapzoneArgument;
import com.doctordark.hcf.eventgame.argument.EventSetLootArgument;
import com.doctordark.hcf.eventgame.argument.EventStartArgument;
import com.doctordark.hcf.eventgame.argument.EventUptimeArgument;
import com.doctordark.util.command.ArgumentExecutor;

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