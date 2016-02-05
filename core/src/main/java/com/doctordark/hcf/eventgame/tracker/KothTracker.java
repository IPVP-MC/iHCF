package com.doctordark.hcf.eventgame.tracker;

import com.doctordark.hcf.DateTimeFormats;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@Deprecated
public class KothTracker implements EventTracker {

    /**
     * Minimum time the KOTH has to be controlled before this tracker will announce when control has been lost.
     */
    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.SECONDS.toMillis(25L);

    public static final long DEFAULT_CAP_MILLIS = TimeUnit.MINUTES.toMillis(15L);

    private final HCF plugin;

    public KothTracker(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public EventType getEventType() {
        return EventType.KOTH;
    }

    @Override
    public void tick(EventTimer eventTimer, EventFaction eventFaction) {
        CaptureZone captureZone = ((KothFaction) eventFaction).getCaptureZone();
        captureZone.updateScoreboardRemaining();
        long remainingMillis = captureZone.getRemainingCaptureMillis();

        if (captureZone.getCappingPlayer() != null
                && !captureZone.getCuboid().contains(captureZone.getCappingPlayer())) {
            captureZone.setCappingPlayer(null);
            onControlLoss(captureZone.getCappingPlayer(), captureZone, eventFaction);
            return;
        }

        if (remainingMillis <= 0L) { // has been captured.
            plugin.getTimerManager().getEventTimer().handleWinner(captureZone.getCappingPlayer());
            eventTimer.clearCooldown();
            return;
        }

        if (remainingMillis == captureZone.getDefaultCaptureMillis()) return;

        int remainingSeconds = (int) (remainingMillis / 1000L);
        if (remainingSeconds > 0 && remainingSeconds % 30 == 0) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[" + eventFaction.getEventType().getDisplayName() + "] " +
                    ChatColor.BLUE + "Someone is controlling " + ChatColor.DARK_AQUA + captureZone.getDisplayName() + ChatColor.BLUE + ". " +
                    ChatColor.RED + '(' + DateTimeFormats.KOTH_FORMAT.format(remainingMillis) + ')');
        }
    }

    @Override
    public void onContest(EventFaction eventFaction, EventTimer eventTimer) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "[" + eventFaction.getEventType().getDisplayName() + "] " +
                ChatColor.DARK_AQUA + eventFaction.getName() + ChatColor.BLUE + " can now be contested. " +
                ChatColor.RED + '(' + DateTimeFormats.KOTH_FORMAT.format(eventTimer.getRemaining()) + ')');
    }

    @Override
    public boolean onControlTake(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        player.sendMessage(ChatColor.DARK_AQUA + "You are now in control of " + ChatColor.BLUE + captureZone.getDisplayName() + ChatColor.DARK_AQUA + '.');
        Bukkit.broadcastMessage(ChatColor.GOLD + "[" + eventFaction.getEventType().getDisplayName() + "] " +
                ChatColor.DARK_AQUA + "Someone" + ChatColor.BLUE + " is in control of " +
                ChatColor.DARK_AQUA + captureZone.getDisplayName() + ChatColor.BLUE + '.' +
                ChatColor.RED + " (" + captureZone.getScoreboardRemaining() + ')');
        return true;
    }

    @Override
    public void onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        player.sendMessage(ChatColor.DARK_AQUA + "You are no longer in control of " + ChatColor.BLUE + captureZone.getDisplayName() + ChatColor.DARK_AQUA + '.');

        // Only broadcast if the KOTH has been controlled for at least 25 seconds to prevent spam.
        long remainingMillis = captureZone.getRemainingCaptureMillis();
        // if (remainingMillis > 0L && captureZone.getDefaultCaptureMillis() - remainingMillis > MINIMUM_CONTROL_TIME_ANNOUNCE) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[" + eventFaction.getEventType().getDisplayName() + "] " +
                    ChatColor.DARK_AQUA + player.getName() + ChatColor.BLUE + " has lost control of " +
                    ChatColor.DARK_AQUA + captureZone.getDisplayName() + ChatColor.BLUE + '.' +
                    ChatColor.RED + " (" + captureZone.getScoreboardRemaining() + ')');
//        }
    }

    @Override
    public void stopTiming() {

    }
}
