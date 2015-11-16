package org.ipvp.hcf.eventgame.conquest;

import com.doctordark.util.command.ArgumentExecutor;
import org.ipvp.hcf.HCF;

public class ConquestExecutor extends ArgumentExecutor {

    public ConquestExecutor(HCF plugin) {
        super("conquest");
        addArgument(new ConquestSetpointsArgument(plugin));
    }
}
