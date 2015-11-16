package org.ipvp.hcf.timer;

import com.doctordark.util.command.ArgumentExecutor;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.timer.argument.TimerCheckArgument;
import org.ipvp.hcf.timer.argument.TimerSetArgument;

/**
 * Handles the execution and tab completion of the timer command.
 */
public class TimerExecutor extends ArgumentExecutor {

    public TimerExecutor(HCF plugin) {
        super("timer");

        addArgument(new TimerCheckArgument(plugin));
        addArgument(new TimerSetArgument(plugin));
    }
}