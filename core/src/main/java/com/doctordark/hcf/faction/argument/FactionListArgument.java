package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.BukkitUtils;
import com.doctordark.util.JavaUtils;
import com.doctordark.util.MapSorting;
import com.doctordark.util.command.CommandArgument;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactionListArgument extends CommandArgument {

    private static final int MAX_FACTIONS_PER_PAGE = 10;

    private final HCF plugin;

    public FactionListArgument(HCF plugin) {
        super("list", "See a list of all factions.");
        this.plugin = plugin;
        this.aliases = new String[]{"l"};
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, final String label, String[] args) {
        final Integer page;
        if (args.length < 2) {
            page = 1;
        } else {
            page = JavaUtils.tryParseInt(args[1]);
            if (page == null) {
                sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a valid number.");
                return true;
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                showList(page, label, sender);
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    private static net.md_5.bungee.api.ChatColor fromBukkit(ChatColor chatColor) {
        return net.md_5.bungee.api.ChatColor.getByChar(chatColor.getChar());
    }

    private void showList(final int pageNumber, final String label, final CommandSender sender) {
        if (pageNumber < 1) {
            sender.sendMessage(ChatColor.RED + "You cannot view a page less than 1.");
            return;
        }

        // Store a map of factions to their online player count.
        Map<PlayerFaction, Integer> factionOnlineMap = new HashMap<>();
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (senderPlayer == null || senderPlayer.canSee(target)) {
                PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(target);
                if (playerFaction != null) {
                    factionOnlineMap.put(playerFaction, factionOnlineMap.getOrDefault(playerFaction, 0) + 1);
                }
            }
        }

        Map<Integer, List<BaseComponent[]>> pages = new HashMap<>();
        List<Map.Entry<PlayerFaction, Integer>> sortedMap = MapSorting.sortedValues(factionOnlineMap, Comparator.reverseOrder());

        for (Map.Entry<PlayerFaction, Integer> entry : sortedMap) {
            int currentPage = pages.size();

            List<BaseComponent[]> results = pages.get(currentPage);
            if (results == null || results.size() >= MAX_FACTIONS_PER_PAGE) {
                pages.put(++currentPage, results = new ArrayList<>(MAX_FACTIONS_PER_PAGE));
            }

            PlayerFaction playerFaction = entry.getKey();
            String displayName = playerFaction.getDisplayName(sender);

            int index = results.size() + (currentPage > 1 ? (currentPage - 1) * MAX_FACTIONS_PER_PAGE : 0) + 1;
            ComponentBuilder builder = new ComponentBuilder("  " + index + ". ").color(net.md_5.bungee.api.ChatColor.WHITE);
            builder.append(displayName).color(net.md_5.bungee.api.ChatColor.RED).
                    event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, '/' + label + " show " + playerFaction.getName())).
                    event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.YELLOW + "Click to view " + displayName + ChatColor.YELLOW + '.')
                            .create()));

            // Show online member counts here.
            builder.append(" [" + entry.getValue() + '/' + playerFaction.getMembers().size() + ']', ComponentBuilder.FormatRetention.FORMATTING).
                    color(net.md_5.bungee.api.ChatColor.GRAY);

            // Show DTR rating here.
            builder.append(" [").color(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
            builder.append(JavaUtils.format(playerFaction.getDeathsUntilRaidable())).color(fromBukkit(playerFaction.getDtrColour()));
            builder.append('/' + JavaUtils.format(playerFaction.getMaximumDeathsUntilRaidable()) + " DTR]").color(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
            results.add(builder.create());
        }

        int maxPages = pages.size();

        if (pageNumber > maxPages) {
            sender.sendMessage(ChatColor.RED + "There " + (maxPages == 1 ? "is only " + maxPages + " page" : "are only " + maxPages + " pages") + ".");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ChatColor.GOLD + " Faction List " + ChatColor.WHITE + "(Page " + pageNumber + '/' + maxPages + ')');

        Player player = sender instanceof Player ? (Player) sender : null;
        Collection<BaseComponent[]> components = pages.get(pageNumber);
        for (BaseComponent[] component : components) {
            if (component == null) continue;
            if (player != null) {
                player.spigot().sendMessage(component);
            } else {
                sender.sendMessage(TextComponent.toPlainText(component));
            }
        }

        sender.sendMessage(ChatColor.GOLD + " You are currently on " + ChatColor.WHITE + "Page " + pageNumber + '/' + maxPages + ChatColor.GOLD + '.');
        sender.sendMessage(ChatColor.GOLD + " To view other pages, use " + ChatColor.YELLOW + '/' + label + ' ' + getName() + " <page#>" + ChatColor.GOLD + '.');
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }
}
