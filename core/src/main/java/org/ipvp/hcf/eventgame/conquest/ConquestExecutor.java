package org.ipvp.hcf.eventgame.conquest;

import org.ipvp.hcf.HCF;
import com.doctordark.util.command.ArgumentExecutor;

public class ConquestExecutor extends ArgumentExecutor {

    public ConquestExecutor(HCF plugin) {
        super("conquest");
        addArgument(new ConquestSetpointsArgument(plugin));
    }
}
