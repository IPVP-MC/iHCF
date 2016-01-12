package com.doctordark.hcf.deathban.lives;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.deathban.lives.argument.LivesCheckArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesCheckDeathbanArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesClearDeathbansArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesGiveArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesReviveArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesSetArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesSetDeathbanTimeArgument;
import com.doctordark.util.command.ArgumentExecutor;

/**
 * Handles the execution and tab completion of the lives command.
 */
public class LivesExecutor extends ArgumentExecutor {

    public LivesExecutor(HCF plugin) {
        super("lives");

        addArgument(new LivesCheckArgument(plugin));
        addArgument(new LivesCheckDeathbanArgument(plugin));
        addArgument(new LivesClearDeathbansArgument(plugin));
        addArgument(new LivesGiveArgument(plugin));
        addArgument(new LivesReviveArgument(plugin));
        addArgument(new LivesSetArgument(plugin));
        addArgument(new LivesSetDeathbanTimeArgument(plugin));
    }
}