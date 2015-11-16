package org.ipvp.hcf.faction.argument;

import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ipvp.hcf.ConfigurationService;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.struct.Role;
import org.ipvp.hcf.faction.type.PlayerFaction;

public class FactionDisbandArgument extends CommandArgument {

    private final HCF plugin;

    public FactionDisbandArgument(HCF plugin) {
        super("disband", "Disband your faction.");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
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

        if (playerFaction.isRaidable() && !ConfigurationService.KIT_MAP && !plugin.getEotwHandler().isEndOfTheWorld()) {
            sender.sendMessage(ChatColor.RED + "You cannot disband your faction while it is raidable.");
            return true;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
            sender.sendMessage(ChatColor.RED + "You must be a leader to disband the faction.");
            return true;
        }

        plugin.getFactionManager().removeFaction(playerFaction, sender);
        return true;
    }
}
