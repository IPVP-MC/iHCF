package com.doctordark.hcf.eventgame.faction;

import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.faction.claim.Claim;
import com.doctordark.hcf.faction.claim.ClaimHandler;
import com.doctordark.hcf.faction.type.ClaimableFaction;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.cuboid.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public abstract class EventFaction extends ClaimableFaction {

    public EventFaction(String name) {
        super(name);
        setDeathban(true); // make cappable factions death-ban between reloads.
    }

    public EventFaction(Map<String, Object> map) {
        super(map);
    }

    @Override
    public String getDisplayName(Faction faction) {
        return ChatColor.AQUA + getName() + ' ' + ChatColor.GOLD + getEventType().getDisplayName();
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        return ChatColor.AQUA + getName();
    }

    public String getScoreboardName() {
        return ChatColor.AQUA + ChatColor.BOLD.toString() + getName();
    }

    /**
     * Sets the {@link Cuboid} area of this {@link KothFaction}.
     *
     * @param cuboid the {@link Cuboid} to set
     * @param sender the {@link CommandSender} setting the claim
     */
    public void setClaim(Cuboid cuboid, CommandSender sender) {
        removeClaims(getClaims(), sender);

        // Now add the new claim.
        Location min = cuboid.getMinimumPoint();
        min.setY(ClaimHandler.MIN_CLAIM_HEIGHT);

        Location max = cuboid.getMaximumPoint();
        max.setY(ClaimHandler.MAX_CLAIM_HEIGHT);

        addClaim(new Claim(this, min, max), sender);
    }

    /**
     * Gets the {@link EventType} of this {@link CapturableFaction}.
     *
     * @return the {@link EventType}
     */
    public abstract EventType getEventType();

    /**
     * Gets the {@link CaptureZone}s for this {@link CapturableFaction}.
     *
     * @return list of {@link CaptureZone}s
     */
    public abstract List<CaptureZone> getCaptureZones();
}
