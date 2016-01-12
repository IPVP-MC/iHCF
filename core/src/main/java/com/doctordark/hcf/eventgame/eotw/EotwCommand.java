package com.doctordark.hcf.eventgame.eotw;

import com.doctordark.hcf.HCF;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import java.util.Collections;
import java.util.List;

/**
 * Command used to set the server in EOTW mode.
 */
public class EotwCommand implements CommandExecutor, TabCompleter {

    private final ConversationFactory factory;

    public EotwCommand(HCF plugin) {
        factory = new ConversationFactory(plugin).
                withFirstPrompt(new EotwPrompt()).
                withEscapeSequence("/no").
                withTimeout(10).
                withModality(false).
                withLocalEcho(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "This command can be only executed from console.");
            return true;
        }

        Conversable conversable = (Conversable) sender;
        conversable.beginConversation(factory.buildConversation(conversable));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    private static final class EotwPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Are you sure you want to do this? The server will be in EOTW mode, If EOTW mode is active, all claims whilst making Spawn a KOTH. " +
                    "You will still have " + EotwHandler.EOTW_WARMUP_WAIT_SECONDS + " seconds to cancel this using the same command though. " +
                    "Type " + ChatColor.GREEN + "yes" + ChatColor.YELLOW + " to confirm or " + ChatColor.RED + "no" + ChatColor.YELLOW + " to deny.";
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String string) {
            if (string.equalsIgnoreCase("yes")) {
                boolean newStatus = !HCF.getPlugin().getEotwHandler().isEndOfTheWorld(false);
                Conversable conversable = context.getForWhom();
                if (conversable instanceof CommandSender) {
                    Command.broadcastCommandMessage((CommandSender) conversable, ChatColor.GOLD + "Set EOTW mode to " + newStatus + '.');
                } else conversable.sendRawMessage(ChatColor.GOLD + "Set EOTW mode to " + newStatus + '.');

                HCF.getPlugin().getEotwHandler().setEndOfTheWorld(newStatus);
            } else if (string.equalsIgnoreCase("no")) {
                context.getForWhom().sendRawMessage(ChatColor.BLUE + "Cancelled the process of setting EOTW mode.");
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + "Unrecognized response. Process of toggling EOTW mode has been cancelled.");
            }

            return Prompt.END_OF_CONVERSATION;
        }
    }
}
