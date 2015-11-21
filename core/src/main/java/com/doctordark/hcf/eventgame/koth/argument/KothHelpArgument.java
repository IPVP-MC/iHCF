package com.doctordark.hcf.eventgame.koth.argument;

import com.doctordark.hcf.eventgame.koth.KothExecutor;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * An {@link CommandArgument} used for viewing help about KingOfTheHill games.
 */
public class KothHelpArgument extends CommandArgument {

    private final KothExecutor kothExecutor;

    public KothHelpArgument(KothExecutor kothExecutor) {
        super("help", "View help about how KOTH's work");
        this.kothExecutor = kothExecutor;
        this.permission = "hcf.command.koth.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.AQUA + "*** KotH Help ***");
        for (CommandArgument argument : kothExecutor.getArguments()) {
            if (argument != this) {
                String permission = argument.getPermission();
                if (permission == null || sender.hasPermission(permission)) {
                    sender.sendMessage(ChatColor.GRAY + argument.getUsage(label) + " - " + argument.getDescription() + '.');
                }
            }
        }

        sender.sendMessage(ChatColor.GRAY + "/fac show <kothName> - View information about a KOTH.");
        return true;
    }
}
