package com.doctordark.hcf.timer;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.argument.TimerCheckArgument;
import com.doctordark.hcf.timer.argument.TimerSetArgument;
import com.doctordark.util.command.ArgumentExecutor;

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