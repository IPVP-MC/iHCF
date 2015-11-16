package org.ipvp.hcf.faction.argument;

import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.claim.Claim;
import org.ipvp.hcf.faction.claim.ClaimHandler;
import org.ipvp.hcf.faction.struct.Role;
import org.ipvp.hcf.faction.type.PlayerFaction;

public class FactionClaimChunkArgument extends CommandArgument {

    private static final int CHUNK_RADIUS = 7;
    private final HCF plugin;

    public FactionClaimChunkArgument(HCF plugin) {
        super("claimchunk", "Claim a chunk of land in the Wilderness.", new String[]{"chunkclaim"});
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

        if (playerFaction.isRaidable()) {
            sender.sendMessage(ChatColor.RED + "You cannot claim land for your faction while raidable.");
            return true;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            sender.sendMessage(ChatColor.RED + "You must be an officer to claim land.");
            return true;
        }

        Location location = player.getLocation();
        plugin.getClaimHandler().tryPurchasing(player, new Claim(playerFaction,
                location.clone().add(CHUNK_RADIUS, ClaimHandler.MIN_CLAIM_HEIGHT, CHUNK_RADIUS),
                location.clone().add(-CHUNK_RADIUS, ClaimHandler.MAX_CLAIM_HEIGHT, -CHUNK_RADIUS)));

        return true;
    }
}
