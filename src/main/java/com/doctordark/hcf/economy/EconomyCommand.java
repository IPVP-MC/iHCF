package com.doctordark.hcf.economy;

import com.doctordark.base.BaseConstants;
import com.doctordark.hcf.HCF;
import com.doctordark.util.BukkitUtils;
import com.doctordark.util.JavaUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Command used to check a players' balance.
 */
public class EconomyCommand implements CommandExecutor, TabCompleter {

    // The max amount of players shown in '/bal top'.
    private static final int MAX_ENTRIES = 10;

    private final HCF plugin;

    public EconomyCommand(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        OfflinePlayer target;
        if (args.length > 0 && sender.hasPermission(command.getPermission() + ".staff")) {
            target = BukkitUtils.offlinePlayerWithNameOrUUID(args[0]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <playerName>");
            return true;
        }

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
            return true;
        }

        UUID uuid = target.getUniqueId();
        int balance = plugin.getEconomyManager().getBalance(uuid);

        if (args.length < 2) {
            sender.sendMessage(ChatColor.GOLD + (sender.equals(target) ? "Your balance" : "Balance of " + target.getName()) + " is " +
                    ChatColor.WHITE + EconomyManager.ECONOMY_SYMBOL + balance + ChatColor.GOLD + '.');

            return true;
        }

        if (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + ' ' + target.getName() + ' ' + args[1] + " <amount>");
                return true;
            }

            Integer amount = JavaUtils.tryParseInt(args[2]);

            if (amount == null) {
                sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid number.");
                return true;
            }

            int newBalance = plugin.getEconomyManager().addBalance(uuid, amount);
            sender.sendMessage(new String[]{
                    ChatColor.YELLOW + "Added " + EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount) + " to balance of " + target.getName() + '.',
                    ChatColor.YELLOW + "Balance of " + target.getName() + " is now " + EconomyManager.ECONOMY_SYMBOL + newBalance + '.'
            });

            return true;
        }

        if (args[1].equalsIgnoreCase("take") || args[1].equalsIgnoreCase("negate") || args[1].equalsIgnoreCase("minus") || args[1].equalsIgnoreCase("subtract")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + ' ' + target.getName() + ' ' + args[1] + " <amount>");
                return true;
            }

            Integer amount = JavaUtils.tryParseInt(args[2]);

            if (amount == null) {
                sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid number.");
                return true;
            }

            int newBalance = plugin.getEconomyManager().subtractBalance(uuid, amount);

            sender.sendMessage(new String[]{
                    ChatColor.YELLOW + "Taken " + EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount) + " from balance of " + target.getName() + '.',
                    ChatColor.YELLOW + "Balance of " + target.getName() + " is now " + EconomyManager.ECONOMY_SYMBOL + newBalance + '.'
            });

            return true;
        }

        if (args[1].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + ' ' + target.getName() + ' ' + args[1] + " <amount>");
                return true;
            }

            Integer amount = JavaUtils.tryParseInt(args[2]);

            if (amount == null) {
                sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid number.");
                return true;
            }

            int newBalance = plugin.getEconomyManager().setBalance(uuid, amount);
            sender.sendMessage(ChatColor.YELLOW + "Set balance of " + target.getName() + " to " + EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(newBalance) + '.');
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + (sender.equals(target) ? "Your balance" : "Balance of " + target.getName()) + " is " + ChatColor.WHITE +
                EconomyManager.ECONOMY_SYMBOL + balance + ChatColor.GOLD + '.');

        return true;
    }

    private static final ImmutableList<String> COMPLETIONS_SECOND = ImmutableList.of("add", "set", "take");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 1:
                List<String> results = Lists.newArrayList("top");
                if (sender.hasPermission(command.getPermission() + ".staff")) {
                    Player senderPlayer = sender instanceof Player ? (Player) sender : null;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (senderPlayer == null || senderPlayer.canSee(player)) {
                            results.add(player.getName());
                        }
                    }
                }

                return BukkitUtils.getCompletions(args, results);
            case 2:
                if (!args[0].equals("top") && sender.hasPermission(command.getPermission() + ".staff")) {
                    return BukkitUtils.getCompletions(args, COMPLETIONS_SECOND);
                }
            default:
                return Collections.emptyList();
        }
    }
}
