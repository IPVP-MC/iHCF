package org.ipvp.hcf.deathban.lives;

import com.doctordark.util.command.ArgumentExecutor;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.deathban.lives.argument.LivesCheckArgument;
import org.ipvp.hcf.deathban.lives.argument.LivesCheckDeathbanArgument;
import org.ipvp.hcf.deathban.lives.argument.LivesClearDeathbansArgument;
import org.ipvp.hcf.deathban.lives.argument.LivesGiveArgument;
import org.ipvp.hcf.deathban.lives.argument.LivesReviveArgument;
import org.ipvp.hcf.deathban.lives.argument.LivesSetArgument;
import org.ipvp.hcf.deathban.lives.argument.LivesSetDeathbanTimeArgument;
import org.ipvp.hcf.deathban.lives.argument.LivesTopArgument;

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
        addArgument(new LivesSetDeathbanTimeArgument());
        addArgument(new LivesTopArgument(plugin));
    }
}