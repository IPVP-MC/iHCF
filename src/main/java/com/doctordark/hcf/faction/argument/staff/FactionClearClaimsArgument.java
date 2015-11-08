package com.doctordark.hcf.faction.argument.staff;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.ClaimableFaction;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
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
 * Faction argument used to set the DTR Regeneration cooldown of {@link Faction}s.
 */
public class FactionClearClaimsArgument extends CommandArgument {

    private final ConversationFactory factory;
    private final HCF plugin;

    public FactionClearClaimsArgument(final HCF plugin) {
        super("clearclaims", "Clears the claims of a faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();

        this.factory = new ConversationFactory(plugin).
                withFirstPrompt(new ClaimClearAllPrompt(plugin)).
                withEscapeSequence("/no").
                withTimeout(10).
                withModality(false).
                withLocalEcho(true);
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName|factionName|all>";
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

        if (faction instanceof ClaimableFaction) {
            ClaimableFaction claimableFaction = (ClaimableFaction) faction;
            claimableFaction.removeClaims(claimableFaction.getClaims(), sender);
            if (claimableFaction instanceof PlayerFaction) {
                ((PlayerFaction) claimableFaction).broadcast(ChatColor.GOLD.toString() + ChatColor.BOLD + "Your claims have been forcefully wiped by " + sender.getName() + '.');
            }
        }

        sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "Claims belonging to " + faction.getName() + " have been forcefully wiped.");
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

    private static class ClaimClearAllPrompt extends StringPrompt {

        private final HCF plugin;

        public ClaimClearAllPrompt(HCF plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Are you sure you want to do this? " + ChatColor.RED + ChatColor.BOLD + "All claims" + ChatColor.YELLOW + " will be cleared. " +
                    "Type " + ChatColor.GREEN + "yes" + ChatColor.YELLOW + " to confirm or " + ChatColor.RED + "no" + ChatColor.YELLOW + " to deny.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String string) {
            switch (string.toLowerCase()) {
                case "yes": {
                    for (Faction faction : plugin.getFactionManager().getFactions()) {
                        if (faction instanceof ClaimableFaction) {
                            ClaimableFaction claimableFaction = (ClaimableFaction) faction;
                            claimableFaction.removeClaims(claimableFaction.getClaims(), Bukkit.getConsoleSender());
                        }
                    }

                    Conversable conversable = context.getForWhom();
                    Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD +
                            "All claims have been cleared" + (conversable instanceof CommandSender ? " by " + ((CommandSender) conversable).getName() : "") + '.');

                    return Prompt.END_OF_CONVERSATION;
                }
                case "no": {
                    context.getForWhom().sendRawMessage(ChatColor.BLUE + "Cancelled the process of clearing all faction claims.");
                    return Prompt.END_OF_CONVERSATION;
                }
                default: {
                    context.getForWhom().sendRawMessage(ChatColor.RED + "Unrecognized response. Process of clearing all faction claims cancelled.");
                    return Prompt.END_OF_CONVERSATION;
                }
            }
        }
    }
}
