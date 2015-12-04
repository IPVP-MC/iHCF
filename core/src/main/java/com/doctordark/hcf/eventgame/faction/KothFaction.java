package com.doctordark.hcf.eventgame.faction;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.type.ClaimableFaction;
import com.doctordark.hcf.faction.type.PlayerFaction;
import com.doctordark.util.BukkitUtils;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Represents a 'King of the Hill' faction.
 */
public class KothFaction extends CapturableFaction implements ConfigurationSerializable {

    private CaptureZone captureZone;

    public KothFaction(String name) {
        super(name);
    }

    public KothFaction(Map<String, Object> map) {
        super(map);
        this.captureZone = (CaptureZone) map.get("captureZone");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("captureZone", captureZone);
        return map;
    }

    @Override
    public List<CaptureZone> getCaptureZones() {
        return captureZone == null ? ImmutableList.of() : ImmutableList.of(captureZone);
    }

    @Override
    public EventType getEventType() {
        return EventType.KOTH;
    }

    @Override
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(getDisplayName(sender));

        for (Claim claim : claims) {
            Location location = claim.getCenter();
            sender.sendMessage(ChatColor.YELLOW + "  Location: " + ChatColor.RED +
                    '(' + ClaimableFaction.ENVIRONMENT_MAPPINGS.get(location.getWorld().getEnvironment()) + ", " + location.getBlockX() + " | " + location.getBlockZ() + ')');
        }

        if (captureZone != null) {
            long remainingCaptureMillis = captureZone.getRemainingCaptureMillis();
            long defaultCaptureMillis = captureZone.getDefaultCaptureMillis();
            if (remainingCaptureMillis > 0L && remainingCaptureMillis != defaultCaptureMillis) {
                sender.sendMessage(ChatColor.YELLOW + "  Remaining Time: " + ChatColor.RED + DurationFormatUtils.formatDurationWords(remainingCaptureMillis, true, true));
            }

            sender.sendMessage(ChatColor.YELLOW + "  Capture Delay: " + ChatColor.RED + captureZone.getDefaultCaptureWords());
            if (captureZone.getCappingPlayer() != null && sender.hasPermission("hcf.koth.checkcapper")) {
                Player capping = captureZone.getCappingPlayer();
                PlayerFaction playerFaction = HCF.getPlugin().getFactionManager().getPlayerFaction(capping);
                String factionTag = "[" + (playerFaction == null ? "*" : playerFaction.getName()) + "]";
                sender.sendMessage(ChatColor.YELLOW + "  Current Capper: " + ChatColor.RED + capping.getName() + ChatColor.GOLD + factionTag);
            }
        }

        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    /**
     * Gets the {@link CaptureZone} of this {@link KothFaction}.
     *
     * @return the {@link CaptureZone} of this {@link KothFaction}
     */
    public CaptureZone getCaptureZone() {
        return captureZone;
    }

    /**
     * Sets the {@link CaptureZone} for this {@link KothFaction}.
     *
     * @param captureZone the {@link CaptureZone} to set
     */
    public void setCaptureZone(CaptureZone captureZone) {
        this.captureZone = captureZone;
    }
}
