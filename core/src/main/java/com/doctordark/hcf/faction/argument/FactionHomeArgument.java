package com.doctordark.hcf.faction.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.faction.FactionExecutor;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.hcf.timer.PlayerTimer;
import com.doctordark.hcf.util.DurationFormatter;
import com.doctordark.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

/**
 * Faction argument used to teleport to {@link Faction} home {@link Location}s.
 */
public class FactionHomeArgument extends CommandArgument {

    private final FactionExecutor factionExecutor;
    private final HCF plugin;

    public FactionHomeArgument(FactionExecutor factionExecutor, HCF plugin) {
        super("home", "Teleport to the faction home.");
        this.factionExecutor = factionExecutor;
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

        if (args.length >= 2 && args[1].equalsIgnoreCase("set")) {
            factionExecutor.getArgument("sethome").onCommand(sender, command, label, args);
            return true;
        }

        UUID uuid = player.getUniqueId();

        PlayerTimer timer = plugin.getTimerManager().getEnderPearlTimer();
        long remaining = timer.getRemaining(player);

        if (remaining > 0L) {
            sender.sendMessage(ChatColor.RED + "You cannot warp whilst your " + timer.getName() + ChatColor.RED + " timer is active.");

            return true;
        }

        if ((remaining = (timer = plugin.getTimerManager().getCombatTimer()).getRemaining(player)) > 0L) {
            sender.sendMessage(ChatColor.RED + "You cannot warp whilst your " + timer.getDisplayName() + ChatColor.RED + " timer is active.");

            return true;
        }

        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(uuid);

        if (playerFaction == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        Location home = playerFaction.getHome();

        if (home == null) {
            sender.sendMessage(ChatColor.RED + "Your faction does not have a home set.");
            return true;
        }

        if (plugin.getConfiguration().getMaxHeightFactionHome() != -1 && home.getY() > plugin.getConfiguration().getMaxHeightFactionHome()) {
            sender.sendMessage(ChatColor.RED + "Your faction home height is above the limit which is " + plugin.getConfiguration().getMaxHeightFactionHome() +
                    ", travel to your current home location at (x. " + home.getBlockX() + ", z. " + home.getBlockZ() + ") and re-set it at a lower height to fix this.");

            return true;
        }

        Faction factionAt = plugin.getFactionManager().getFactionAt(player.getLocation());

        if (factionAt instanceof EventFaction) {
            sender.sendMessage(ChatColor.RED + "You cannot warp whilst in event zones.");
            return true;
        }

        if (factionAt != playerFaction && factionAt instanceof PlayerFaction && plugin.getConfiguration().isAllowTeleportingInEnemyTerritory()) {
            player.sendMessage(ChatColor.RED + "You may not warp in enemy claims. Use " + ChatColor.YELLOW + '/' + label + " stuck" + ChatColor.RED + " if trapped.");
            return true;
        }

        long millis;
        if (factionAt.isSafezone()) {
            millis = 0L;
        } else {
            String name;
            switch (player.getWorld().getEnvironment()) {
                case THE_END:
                    name = "End";
                    millis = plugin.getConfiguration().getFactionHomeTeleportDelayEndMillis();
                    break;
                case NETHER:
                    name = "Nether";
                    millis = plugin.getConfiguration().getFactionHomeTeleportDelayNetherMillis();
                    break;
                case NORMAL:
                    name = "Overworld";
                    millis = plugin.getConfiguration().getFactionHomeTeleportDelayOverworldMillis();
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognised environment");
            }

            if (millis == -1L) {
                sender.sendMessage(ChatColor.RED + "Home teleports are disabled in the " + name + ".");
                return true;
            }
        }

        if (factionAt != playerFaction && factionAt instanceof PlayerFaction) {
            millis *= 2L;
        }

        plugin.getTimerManager().getTeleportTimer().teleport(player, home, millis,
                ChatColor.AQUA + "Teleporting to your faction home in " + ChatColor.LIGHT_PURPLE + DurationFormatter.getRemaining(millis, true, false) + ChatColor.AQUA + ". Do not move or take damage.",
                PlayerTeleportEvent.TeleportCause.COMMAND);

        return true;
    }
}
