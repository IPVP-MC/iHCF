package org.ipvp.hcf.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.ipvp.hcf.ConfigurationService;
import org.ipvp.hcf.HCF;
import org.ipvp.hcf.faction.event.FactionClaimChangeEvent;
import org.ipvp.hcf.faction.event.cause.ClaimChangeCause;
import org.ipvp.hcf.faction.type.PlayerFaction;

public class KitMapListener implements Listener {

    final HCF plugin;

    public KitMapListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionClaimChange(FactionClaimChangeEvent event) {
        if (event.getCause() == ClaimChangeCause.CLAIM && ConfigurationService.KIT_MAP && event.getClaimableFaction() instanceof PlayerFaction) {
            event.setCancelled(true);
            event.getSender().sendMessage(ChatColor.RED + "Player based land cannot be claimed during a kit map.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (ConfigurationService.KIT_MAP) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (ConfigurationService.KIT_MAP && plugin.getFactionManager().getFactionAt(event.getItemDrop().getLocation()).isSafezone()) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (ConfigurationService.KIT_MAP && plugin.getFactionManager().getFactionAt(event.getLocation()).isSafezone()) {
            event.getEntity().remove();
        }
    }
}

