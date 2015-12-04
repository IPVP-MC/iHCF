package com.doctordark.hcf.faction.argument.staff;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Faction argument used to forcefully remove {@link Faction}s.
 */
public class FactionRemoveArgument extends CommandArgument {

    private final ConversationFactory factory;
    private final HCF plugin;

    public FactionRemoveArgument(final HCF plugin) {
        super("remove", "Remove a faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"delete", "forcedisband", "forceremove"};
        this.permission = "hcf.command.faction.argument." + getName();
        this.factory = new ConversationFactory(plugin).
                withFirstPrompt(new RemoveAllPrompt(plugin)).
                withEscapeSequence("/no").
                withTimeout(10).
                withModality(false).
                withLocalEcho(true);
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <all|factionName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        if (args[1].equalsIgnoreCase("all")) {
            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(ChatColor.RED + "This command can be only executed from console.");
                return true;
            }

            Conversable conversable = (Conversable) sender;
            conversable.beginConversation(factory.buildConversation(conversable));
            return true;
        }

        Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);

        if (faction == null) {
            sender.sendMessage(ChatColor.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
            return true;
        }

        if (plugin.getFactionManager().removeFaction(faction, sender)) {
            Command.broadcastCommandMessage(sender, ChatColor.YELLOW + "Disbanded faction " + faction.getName() + ChatColor.YELLOW + '.');
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        } else if (args[1].isEmpty()) {
            return null;
        } else {
            Player player = (Player) sender;
            List<String> results = new ArrayList<>(plugin.getFactionManager().getFactionNameMap().keySet());
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (player.canSee(target) && !results.contains(target.getName())) {
                    results.add(target.getName());
                }
            }

            return results;
        }
    }

    private static class RemoveAllPrompt extends StringPrompt {

        private final HCF plugin;

        public RemoveAllPrompt(HCF plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Are you sure you want to do this? " + ChatColor.RED + ChatColor.BOLD + "All factions" + ChatColor.YELLOW + " will be cleared. " +
                    "Type " + ChatColor.GREEN + "yes" + ChatColor.YELLOW + " to confirm or " + ChatColor.RED + "no" + ChatColor.YELLOW + " to deny.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String string) {
            switch (string.toLowerCase()) {
                case "yes": {
                    for (Faction faction : plugin.getFactionManager().getFactions()) {
                        plugin.getFactionManager().removeFaction(faction, Bukkit.getConsoleSender());
                    }

                    Conversable conversable = context.getForWhom();
                    Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD +
                            "All factions have been disbanded" + (conversable instanceof CommandSender ? " by " + ((CommandSender) conversable).getName() : "") + '.');

                    return Prompt.END_OF_CONVERSATION;
                }
                case "no": {
                    context.getForWhom().sendRawMessage(ChatColor.BLUE + "Cancelled the process of disbanding all factions.");
                    return Prompt.END_OF_CONVERSATION;
                }
                default: {
                    context.getForWhom().sendRawMessage(ChatColor.RED + "Unrecognized response. Process of disbanding all factions cancelled.");
                    return Prompt.END_OF_CONVERSATION;
                }
            }
        }
    }
}
