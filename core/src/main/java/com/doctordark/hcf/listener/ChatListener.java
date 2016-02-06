package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.event.FactionChatEvent;
import com.doctordark.hcf.faction.struct.ChatChannel;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;

public class ChatListener implements Listener {

    private Essentials essentials;
    private final HCF plugin;

    public ChatListener(HCF plugin) {
        this.plugin = plugin;

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin essentialsPlugin = pluginManager.getPlugin("Essentials");
        if (essentialsPlugin instanceof Essentials && essentialsPlugin.isEnabled()) {
            this.essentials = (Essentials) essentialsPlugin;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        ChatChannel chatChannel = playerFaction == null ? ChatChannel.PUBLIC : playerFaction.getMember(player).getChatChannel();

        // Handle faction or alliance chat modes.
        Set<Player> recipients = event.getRecipients();
        if (chatChannel == ChatChannel.FACTION || chatChannel == ChatChannel.ALLIANCE) {
            if (isGlobalChannel(message)) { // allow players to use '!' to bypass friendly chat.
                message = message.substring(1, message.length()).trim();
                event.setMessage(message);
            } else {
                Collection<Player> online = playerFaction.getOnlinePlayers();
                if (chatChannel == ChatChannel.ALLIANCE) {
                    Collection<PlayerFaction> allies = playerFaction.getAlliedFactions();
                    for (PlayerFaction ally : allies) {
                        online.addAll(ally.getOnlinePlayers());
                    }
                }

                recipients.retainAll(online);
                event.setFormat(chatChannel.getRawFormat(player));

                Bukkit.getPluginManager().callEvent(new FactionChatEvent(true, playerFaction, player, chatChannel, recipients, event.getMessage()));
                return;
            }
        }

        String displayName = player.getDisplayName();
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String defaultFormat = this.getChatFormat(player, playerFaction, console);

        // Handle the custom messaging here.
        event.setFormat(defaultFormat);
        event.setCancelled(true);
        console.sendMessage(String.format(defaultFormat, displayName, message));
        for (Player recipient : event.getRecipients()) {
            recipient.sendMessage(String.format(this.getChatFormat(player, playerFaction, recipient), displayName, message));
        }
    }

    private String getChatFormat(Player player, PlayerFaction playerFaction, CommandSender viewer) {
        String factionTag = playerFaction == null ? ChatColor.RED + Faction.FACTIONLESS_PREFIX : playerFaction.getDisplayName(viewer);
        String capperTag = plugin.getConfiguration().getEotwLastMapCapperUuids().contains(player.getUniqueId().toString()) ? plugin.getConfiguration().getEotwChatSymbolPrefix() : "";
        String result;
        if (this.essentials != null) {
            User user = this.essentials.getUser(player);
            result = this.essentials.getSettings().getChatFormat(user.getGroup())
                    .replace("{FACTION}", Matcher.quoteReplacement(factionTag))
                    .replace("{EOTWCAPPERPREFIX}", Matcher.quoteReplacement(capperTag))
                    .replace("{DISPLAYNAME}", Matcher.quoteReplacement(user.getDisplayName()))
                    .replace("{MESSAGE}", Matcher.quoteReplacement("%2$s"));
        } else {
            result = ChatColor.GOLD + "[" + factionTag + ChatColor.GOLD + "] " + capperTag + "%1$s" + ChatColor.GRAY + ": " + ChatColor.WHITE + "%2$s";
        }

        return result;
    }

    /**
     * Checks if a message should be posted in {@link ChatChannel#PUBLIC}.
     *
     * @param input the message to check
     * @return true if the message should be posted in {@link ChatChannel#PUBLIC}
     */
    private boolean isGlobalChannel(String input) {
        int length = input.length();
        if (length > 1 && input.startsWith("!")) {
            for (int i = 1 ; i < length ; i++) {
                char character = input.charAt(i);

                // Ignore whitespace to prevent blank messages
                if (Character.isWhitespace(character)) {
                    continue;
                }

                // Player is faking a command
                if (character == '/') {
                    return false;
                }

                break;
            }
        }

        return true;
    }
}
