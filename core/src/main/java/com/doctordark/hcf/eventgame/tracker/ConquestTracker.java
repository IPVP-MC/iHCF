package com.doctordark.hcf.eventgame.tracker;

import com.doctordark.base.GuavaCompat;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.eventgame.faction.ConquestFaction;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.faction.event.FactionRemoveEvent;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.hcf.util.ConcurrentValueOrderedMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tracker used for handling the Conquest points.
 */
@Deprecated
public class ConquestTracker implements EventTracker, Listener {

    /**
     * Minimum time the KOTH has to be controlled before this tracker will announce when control has been lost.
     */
    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.SECONDS.toMillis(5L);
    public static final long DEFAULT_CAP_MILLIS = TimeUnit.SECONDS.toMillis(30L);

    private final ConcurrentValueOrderedMap<PlayerFaction, Integer> factionPointsMap = new ConcurrentValueOrderedMap<>();
    private final HCF plugin;

    public ConquestTracker(HCF plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(FactionRemoveEvent event) {
        Faction faction = event.getFaction();
        if (faction instanceof PlayerFaction) {
            this.factionPointsMap.remove((PlayerFaction) faction);
        }
    }

    /**
     * Gets the map containing the points for all factions.
     *
     * @return immutable copy of the faction points map
     */
    public ConcurrentValueOrderedMap<PlayerFaction, Integer> getFactionPointsMap() {
        return this.factionPointsMap;
    }

    /**
     * Gets the amount of points a {@link PlayerFaction} has
     * gained for this {@link ConquestTracker}.
     *
     * @param faction the faction to get for
     * @return the new points of the {@link PlayerFaction}.
     */
    public int getPoints(PlayerFaction faction) {
        return GuavaCompat.firstNonNull(this.factionPointsMap.get(faction), 0);
    }

    /**
     * Sets the points a {@link PlayerFaction} has gained for this {@link ConquestTracker}.
     *
     * @param faction the faction to set for
     * @param amount  the amount to set
     * @return the new points of the {@link PlayerFaction}
     */
    public int setPoints(PlayerFaction faction, int amount) {
        this.factionPointsMap.put(faction, amount);
        return amount;
    }

    /**
     * Takes points from a {@link PlayerFaction} gained from this {@link ConquestTracker}.has
     *
     * @param faction the faction to take from
     * @param amount  the amount to take
     * @return the new points of the {@link PlayerFaction}
     */
    public int takePoints(PlayerFaction faction, int amount) {
        return setPoints(faction, getPoints(faction) - amount);
    }

    /**
     * Adds points to a {@link PlayerFaction} gained from this {@link ConquestTracker}.has
     *
     * @param faction the faction to add from
     * @param amount  the amount to add
     * @return the new points of the {@link PlayerFaction}
     */
    public int addPoints(PlayerFaction faction, int amount) {
        return setPoints(faction, getPoints(faction) + amount);
    }

    @Override
    public EventType getEventType() {
        return EventType.CONQUEST;
    }

    @Override
    public void tick(EventTimer eventTimer, EventFaction eventFaction) {
        ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
        List<CaptureZone> captureZones = conquestFaction.getCaptureZones();
        for (CaptureZone captureZone : captureZones) {
            captureZone.updateScoreboardRemaining();
            Player cappingPlayer = captureZone.getCappingPlayer();
            if (cappingPlayer == null) continue;

            if (!captureZone.getCuboid().contains(cappingPlayer)) {
                onControlLoss(cappingPlayer, captureZone, eventFaction);
                continue;
            }

            // The capture zone has been controlled.
            long remainingMillis = captureZone.getRemainingCaptureMillis();
            if (remainingMillis <= 0L) {
                UUID uuid = cappingPlayer.getUniqueId();

                PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(uuid);
                if (playerFaction != null) {
                    int newPoints = addPoints(playerFaction, 1);
                    if (newPoints < plugin.getConfiguration().getConquestRequiredVictoryPoints()) {
                        // Reset back to the default for this tracker.
                        captureZone.setRemainingCaptureMillis(captureZone.getDefaultCaptureMillis());
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "[" + eventFaction.getName() + "] " +
                                ChatColor.LIGHT_PURPLE + ChatColor.BOLD + playerFaction.getName() +
                                ChatColor.GOLD + " gained " + 1 + " point for capturing " + captureZone.getDisplayName() + ChatColor.GOLD + ". " +
                                ChatColor.AQUA + '(' + newPoints + '/' + plugin.getConfiguration().getConquestRequiredVictoryPoints() + ')');
                    } else {
                        // Clear all the points for the next Conquest event.
                        this.factionPointsMap.clear();
                        plugin.getTimerManager().getEventTimer().handleWinner(cappingPlayer);
                        return;
                    }
                }
                return;
            }

            int remainingSeconds = (int) Math.round((double) remainingMillis / 1000L);
            if (remainingSeconds % 5 == 0) {
                cappingPlayer.sendMessage(ChatColor.YELLOW + "[" + eventFaction.getName() + "] " + ChatColor.GOLD +
                        "Attempting to control " + ChatColor.YELLOW + captureZone.getDisplayName() + ChatColor.GOLD + ". " +
                        ChatColor.YELLOW + '(' + remainingSeconds + "s)");
            }
        }
    }

    @Override
    public void onContest(EventFaction eventFaction, EventTimer eventTimer) {
        Bukkit.broadcastMessage(ChatColor.YELLOW + (eventFaction instanceof ConquestFaction ? "" : "[" + eventFaction.getName() + "] ") + ChatColor.GOLD + eventFaction.getName() + " can now be contested.");
    }

    @Override
    public boolean onControlTake(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        if (plugin.getFactionManager().getPlayerFaction(player.getUniqueId()) == null) {
            player.sendMessage(ChatColor.RED + "You must be in a faction to capture for Conquest.");
            return false;
        }

        return true;
    }

    @Override
    public void onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        long remainingMillis = captureZone.getRemainingCaptureMillis();
        if (remainingMillis > 0L && captureZone.getDefaultCaptureMillis() - remainingMillis > MINIMUM_CONTROL_TIME_ANNOUNCE) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "[" + eventFaction.getName() + "] " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + player.getName() +
                    ChatColor.GOLD + " was knocked off " + captureZone.getDisplayName() + ChatColor.GOLD + '.');
        }
    }

    @Override
    public void stopTiming() {
        factionPointsMap.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Faction currentEventFac = plugin.getTimerManager().getEventTimer().getEventFaction();
        if (currentEventFac instanceof ConquestFaction) {
            Player player = event.getEntity();
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction != null && plugin.getConfiguration().getConquestPointLossPerDeath() > 0) {
                int oldPoints = getPoints(playerFaction);
                if (oldPoints == 0) return;

                int newPoints = takePoints(playerFaction, plugin.getConfiguration().getConquestPointLossPerDeath());
                event.setDeathMessage(null); // for some reason if it isn't handled manually, weird colour coding happens
                Bukkit.broadcastMessage(ChatColor.YELLOW + "[" + currentEventFac.getName() + "] " +
                        ChatColor.GOLD + playerFaction.getName() + " lost " +
                        plugin.getConfiguration().getConquestPointLossPerDeath() + " points because " + player.getName() + " died." +
                        ChatColor.AQUA + " (" + newPoints + '/' + plugin.getConfiguration().getConquestRequiredVictoryPoints() + ')' + ChatColor.YELLOW + '.');
            }
        }
    }
}
