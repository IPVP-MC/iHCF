package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.struct.ChatChannel;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FactionChatArgument extends CommandArgument {

    private final HCF plugin;

    public FactionChatArgument(HCF plugin) {
        super("chat", "Toggle faction chat only mode on or off.", new String[]{"c"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " [fac|public|ally] [message]";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        FactionMember member = playerFaction.getMember(player.getUniqueId());
        ChatChannel currentChannel = member.getChatChannel();
        ChatChannel parsed = args.length >= 2 ? ChatChannel.parse(args[1], null) : currentChannel.getRotation();

        if (parsed == null && currentChannel != ChatChannel.PUBLIC) {
            Collection<Player> recipients = playerFaction.getOnlinePlayers();
            if (currentChannel == ChatChannel.ALLIANCE) {
                for (PlayerFaction ally : playerFaction.getAlliedFactions()) {
                    recipients.addAll(ally.getOnlinePlayers());
                }
            }

            String format = String.format(currentChannel.getRawFormat(player), "", HCF.SPACE_JOINER.join(Arrays.copyOfRange(args, 1, args.length)));
            for (Player recipient : recipients) {
                recipient.sendMessage(format);
            }

            // spawn radius, border, allies, minigames,
            return true;
        }

        ChatChannel newChannel = parsed == null ? currentChannel.getRotation() : parsed;
        member.setChatChannel(newChannel);

        sender.sendMessage(ChatColor.YELLOW + "You are now in " + ChatColor.AQUA + newChannel.getDisplayName().toLowerCase() + ChatColor.YELLOW + " chat mode.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        ChatChannel[] values = ChatChannel.values();
        List<String> results = new ArrayList<>(values.length);
        for (ChatChannel type : values) {
            results.add(type.getName());
        }

        return results;
    }
}
