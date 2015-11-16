package org.ipvp.hcf.deathban.lives.argument;

import com.doctordark.util.command.CommandArgument;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.procedure.TObjectIntProcedure;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.ipvp.hcf.HCF;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * An {@link CommandArgument} used to check who has the most lives.
 */
public class LivesTopArgument extends CommandArgument {

    private static final int MAX_ENTRIES = 10;
    private final HCF plugin;

    public LivesTopArgument(HCF plugin) {
        super("top", "Check who has the most lives");
        this.plugin = plugin;
        this.permission = "hcf.command.lives.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        synchronized (plugin.getDeathbanManager().getLivesMap()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    TObjectIntMap<UUID> livesMap = plugin.getDeathbanManager().getLivesMap();

                    if (livesMap.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "There are no lives stored.");
                        return;
                    }

                    sender.sendMessage(ChatColor.GOLD + "Top " + MAX_ENTRIES + " Lives.");
                    livesMap.forEachEntry(new TObjectIntProcedure<UUID>() {
                        int count = 0;

                        @Override
                        public boolean execute(UUID uuid, int balance) {
                            OfflinePlayer offlineNext = Bukkit.getOfflinePlayer(uuid);
                            sender.sendMessage(" " + ChatColor.GRAY + (++count) + ". " + ChatColor.YELLOW + offlineNext.getName() + ": " + ChatColor.WHITE + balance);
                            return count != MAX_ENTRIES;
                        }
                    });
                }
            }.runTaskAsynchronously(plugin);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
