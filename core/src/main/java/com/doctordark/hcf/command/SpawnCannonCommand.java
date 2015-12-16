package com.doctordark.hcf.command;

import com.doctordark.hcf.ConfigurationService;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.WarzoneFaction;
import com.doctordark.util.BukkitUtils;
import com.doctordark.util.JavaUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Command used to launch or aim at a position within the Warzone.
 */
public class SpawnCannonCommand implements CommandExecutor, TabCompleter {

    private static final Material SPAWN_CANNON_BLOCK = Material.BEACON;

    private final HCF plugin;

    public SpawnCannonCommand(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use the spawn cannon.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <launch|aim [x z])>");
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            sender.sendMessage(ChatColor.RED + "You can only use the spawn cannon in the overworld.");
            return true;
        }

        Location location = player.getLocation();

        if (location.getBlock().getRelative(BlockFace.DOWN).getType() != SPAWN_CANNON_BLOCK) {
            sender.sendMessage(ChatColor.RED + "You are not on a spawn cannon (" + ChatColor.AQUA + SPAWN_CANNON_BLOCK.name() + ChatColor.RED + ").");
            return true;
        }

        if (!plugin.getFactionManager().getFactionAt(location).isSafezone()) {
            sender.sendMessage(ChatColor.RED + "You can only use the spawn cannon in safe-zones.");
            return true;
        }

        if (args[0].equalsIgnoreCase("aim")) {
            if (!sender.hasPermission(command.getPermission() + ".aim")) {
                sender.sendMessage(ChatColor.RED + "You do not have access to aim the spawn cannon.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + ' ' + args[0].toLowerCase() + " <x> <z>");
                return true;
            }

            Integer x = JavaUtils.tryParseInt(args[1]);
            Integer z; // lazy load
            if (x == null || (z = JavaUtils.tryParseInt(args[2])) == null) {
                sender.sendMessage(ChatColor.RED + "Your x or z co-ordinate was invalid.");
                return true;
            }

            launchPlayer(player, new Location(world, x, 0, z));
            return true;
        }

        if (args[0].equalsIgnoreCase("launch")) {
            if (!sender.hasPermission(command.getPermission() + ".launch")) {
                sender.sendMessage(ChatColor.RED + "You do not have access to launch with the spawn cannon.");
                return true;
            }

            int min = ConfigurationService.SPAWN_RADIUS_MAP.get(world.getEnvironment());
            int max = plugin.getConfiguration().getWarzoneRadiusOverworld();
            int maxCannonDistance = getMaxCannonDistance(sender);
            Random random = plugin.getRandom();

            int x = Math.max(random.nextInt(Math.min(max, maxCannonDistance)), min);
            if (random.nextBoolean()) x = -x;

            int z = Math.max(random.nextInt(Math.min(max, maxCannonDistance)), min);
            if (random.nextBoolean()) z = -z;

            launchPlayer(player, new Location(world, x, 0, z));
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <launch|aim [x z])>");
        return true;
    }

    /**
     * Launches the player into the warzone from the spawn cannon.
     *
     * @param player   the player to launch
     * @param location the expected launch landing location
     */
    public void launchPlayer(Player player, Location location) {
        Faction factionAt = plugin.getFactionManager().getFactionAt(location);

        if (!(factionAt instanceof WarzoneFaction)) {
            player.sendMessage(ChatColor.RED + "You can only cannon to areas in the Warzone.");
            return;
        }

        int x = location.getBlockX();
        int z = location.getBlockZ();

        int maxDistance = getMaxCannonDistance(player);

        if (Math.abs(x) > maxDistance || Math.abs(z) > maxDistance) {
            player.sendMessage(ChatColor.RED + "You cannot launch that far from the spawn cannon. Your limit is " + maxDistance + '.');
            return;
        }

        location = BukkitUtils.getHighestLocation(location).add(0, 3, 0);
        player.sendMessage(ChatColor.YELLOW + "Cannoning to " + ChatColor.GREEN + x + ", " + z + ChatColor.YELLOW + '.');
        player.playSound(location, Sound.ENDERMAN_TELEPORT, 1, 1);
        player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1));
    }

    /**
     * Gets the maximum cannon distance a player can go too.
     *
     * @param sender the sender to check for
     * @return the maximum cannon distance for player
     */
    public int getMaxCannonDistance(CommandSender sender) {
        int decrement = 50;
        int radius = ((plugin.getConfiguration().getWarzoneRadiusOverworld() + decrement - 1) / decrement) * decrement;
        for (int i = radius; i > 0; i -= decrement) {
            if (sender.hasPermission("hcf.spawncannon." + i)) {
                return i;
            }
        }

        return 100;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 1 ? BukkitUtils.getCompletions(args, COMPLETIONS) : Collections.emptyList();
    }

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("aim", "launch");
}
