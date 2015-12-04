package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.event.FactionChatEvent;
import com.doctordark.hcf.faction.struct.ChatChannel;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.google.common.collect.MapMaker;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private static final String DOUBLE_POST_BYPASS_PERMISSION = "hcf.doublepost.bypass";
    private static final Pattern PATTERN = Pattern.compile("\\W");

    private Essentials essentials;
    private final Map<UUID, String> messageHistory;
    private final HCF plugin;

    public ChatListener(HCF plugin) {
        this.plugin = plugin;

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin essentialsPlugin = pluginManager.getPlugin("Essentials");
        if (essentialsPlugin instanceof Essentials && essentialsPlugin.isEnabled()) {
            this.essentials = (Essentials) essentialsPlugin;
        }

        this.messageHistory = new MapMaker().expireAfterWrite(2, TimeUnit.MINUTES).makeMap();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        // Prevent double posting.
        String lastMessage = messageHistory.get(player.getUniqueId());
        String cleanedMessage = PATTERN.matcher(message).replaceAll("");
        if (lastMessage != null && (message.equals(lastMessage) || StringUtils.getLevenshteinDistance(cleanedMessage, lastMessage) <= 1) &&
                !player.hasPermission(DOUBLE_POST_BYPASS_PERMISSION)) {

            player.sendMessage(ChatColor.RED + "Hey, don't double post.");
            event.setCancelled(true);
            return;
        } else {
            messageHistory.put(player.getUniqueId(), cleanedMessage);
        }

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
            result = this.essentials.getSettings().getChatFormat(user.getGroup());
            result = result.replace("{FACTION}", factionTag).replace("{EOTWCAPPERPREFIX}", capperTag).replace("{DISPLAYNAME}", user.getDisplayName()).replace("{MESSAGE}", "%2$s");
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
        if (length <= 1 || !input.startsWith("!")) {
            return false;
        }

        for (int i = 1; i < length; i++) {
            char character = input.charAt(i);
            if (character == ' ') continue;
            if (character == '/') {
                return false;
            } else {
                break;
            }
        }

        return true;
    }
}
