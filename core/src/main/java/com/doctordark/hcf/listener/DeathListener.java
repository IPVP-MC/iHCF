package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.struct.Role;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.hcf.user.FactionUser;
import com.doctordark.util.JavaUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final HCF plugin;

    public DeathListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDeathKillIncrement(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            FactionUser user = plugin.getUserManager().getUser(killer.getUniqueId());
            user.setKills(user.getKills() + 1);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) return;

        Faction factionAt = plugin.getFactionManager().getFactionAt(player.getLocation());
        double dtrLoss = (1.0D * factionAt.getDtrLossMultiplier());
        double newDtr = playerFaction.setDeathsUntilRaidable(playerFaction.getDeathsUntilRaidable() - dtrLoss);

        Role role = playerFaction.getMember(player.getUniqueId()).getRole();
        long baseDelay = plugin.getConfiguration().getFactionDtrRegenFreezeBaseMilliseconds();
        playerFaction.setRemainingRegenerationTime(baseDelay + (playerFaction.getOnlinePlayers().size() * plugin.getConfiguration().getFactionDtrRegenFreezeMillisecondsPerMember()));
        playerFaction.broadcast(ChatColor.GOLD + "Member Death: " + plugin.getConfiguration().getRelationColourTeammate() +
                role.getAstrix() + player.getName() + ChatColor.GOLD + ". " +
                "DTR: (" + ChatColor.WHITE + JavaUtils.format(newDtr, 2) + '/' + JavaUtils.format(playerFaction.getMaximumDeathsUntilRaidable(), 2) + ChatColor.GOLD + ").");
    }

}
