package com.doctordark.hcf.eventgame.conquest;

import com.doctordark.hcf.HCF;
import com.doctordark.util.command.ArgumentExecutor;

public class ConquestExecutor extends ArgumentExecutor {

    public ConquestExecutor(HCF plugin) {
        super("conquest");
        addArgument(new ConquestSetpointsArgument(plugin));
    }
}
