package org.ipvp.hcf.faction.argument.staff;

import com.doctordark.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.claim.ClaimSelection;
import org.ipvp.hcf.faction.type.ClaimableFaction;
import org.ipvp.hcf.faction.type.Faction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Used to claim land for other {@link ClaimableFaction}s.
 */
public class FactionClaimForArgument extends CommandArgument {

    private final HCF plugin;

    public FactionClaimForArgument(HCF plugin) {
        super("claimfor", "Claims land for another faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <factionName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        Faction targetFaction = plugin.getFactionManager().getFaction(args[1]);

        if (!(targetFaction instanceof ClaimableFaction)) {
            sender.sendMessage(ChatColor.RED + "Claimable faction named " + args[1] + " not found.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        ClaimSelection claimSelection = plugin.getClaimHandler().claimSelectionMap.get(uuid);

        if (claimSelection == null || !claimSelection.hasBothPositionsSet()) {
            player.sendMessage(ChatColor.RED + "You have not set both positions of this claim selection.");
            return true;
        }

        if (plugin.getClaimHandler().tryPurchasing(player, claimSelection.toClaim(targetFaction))) {
            plugin.getClaimHandler().clearClaimSelection(player);
            player.setItemInHand(new ItemStack(Material.AIR, 1));
            sender.sendMessage(ChatColor.YELLOW + "Successfully claimed this land for " + ChatColor.RED + targetFaction.getName() + ChatColor.YELLOW + '.');
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args[1].isEmpty()) {
            return null;
        }

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
