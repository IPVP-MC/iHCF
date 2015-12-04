package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.economy.EconomyManager;
import com.doctordark.hcf.faction.FactionMember;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.JavaUtils;
import com.doctordark.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionWithdrawArgument extends CommandArgument {

    private final HCF plugin;

    public FactionWithdrawArgument(HCF plugin) {
        super("withdraw", "Withdraws money from the faction balance.", new String[]{"w"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <all|amount>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can update the faction balance.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        FactionMember factionMember = playerFaction.getMember(uuid);

        if (factionMember.getRole() == Role.MEMBER) {
            sender.sendMessage(ChatColor.RED + "You must be a faction officer to withdraw money.");
            return true;
        }

        int factionBalance = playerFaction.getBalance();
        Integer amount;

        if (args[1].equalsIgnoreCase("all")) {
            amount = factionBalance;
        } else {
            if ((amount = (JavaUtils.tryParseInt(args[1]))) == null) {
                sender.sendMessage(ChatColor.RED + "Error: '" + args[1] + "' is not a valid number.");
                return true;
            }
        }

        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be positive.");
            return true;
        }

        if (amount > factionBalance) {
            sender.sendMessage(ChatColor.RED + "Your faction need at least " + EconomyManager.ECONOMY_SYMBOL +
                    JavaUtils.format(amount) + " to do this, whilst it only has " + EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(factionBalance) + '.');

            return true;
        }

        plugin.getEconomyManager().addBalance(uuid, amount);
        playerFaction.setBalance(factionBalance - amount);
        playerFaction.broadcast(plugin.getConfiguration().getRelationColourTeammate() + factionMember.getRole().getAstrix() + sender.getName() + ChatColor.YELLOW + " has withdrew " +
                ChatColor.BOLD + EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount) + ChatColor.YELLOW + " from the faction balance.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? COMPLETIONS : Collections.<String>emptyList();
    }

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
}
