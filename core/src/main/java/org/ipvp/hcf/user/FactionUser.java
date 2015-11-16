package org.ipvp.hcf.user;

import com.doctordark.util.GenericUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.ipvp.hcf.deathban.Deathban;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FactionUser implements ConfigurationSerializable {

    private final Set<UUID> factionChatSpying = new HashSet<>();
    private final Set<String> shownScoreboardScores = new HashSet<>();

    private final UUID userUUID;

    private boolean capzoneEntryAlerts;
    private boolean showClaimMap;
    private boolean showLightning = true;
    private Deathban deathban;
    private long lastFactionLeaveMillis;
    private int kills;
    private int deaths;

    public FactionUser(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public FactionUser(Map<String, Object> map) {
        this.shownScoreboardScores.addAll(GenericUtils.createList(map.get("shownScoreboardScores"), String.class));
        this.factionChatSpying.addAll(GenericUtils.createList(map.get("faction-chat-spying"), String.class).stream().map(UUID::fromString).collect(Collectors.toList()));
        this.userUUID = UUID.fromString((String) map.get("userUUID"));
        this.capzoneEntryAlerts = (Boolean) map.get("capzoneEntryAlerts");
        //this.showClaimMap = (Boolean) map.get("showClaimMap");
        this.showLightning = (Boolean) map.get("showLightning");
        this.deathban = (Deathban) map.get("deathban");
        this.lastFactionLeaveMillis = Long.parseLong((String) map.get("lastFactionLeaveMillis"));
        this.kills = (Integer) map.get("kills");
        this.deaths = (Integer) map.get("deaths");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("shownScoreboardScores", new ArrayList<>(shownScoreboardScores));
        map.put("faction-chat-spying", factionChatSpying.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("userUUID", userUUID.toString());
        map.put("capzoneEntryAlerts", capzoneEntryAlerts);
        map.put("showClaimMap", showClaimMap);
        map.put("showLightning", showLightning);
        map.put("deathban", deathban);
        map.put("lastFactionLeaveMillis", Long.toString(lastFactionLeaveMillis));
        map.put("kills", kills);
        map.put("deaths", deaths);
        return map;
    }

    public boolean isCapzoneEntryAlerts() {
        return capzoneEntryAlerts;
    }

    public void setCapzoneEntryAlerts(boolean capzoneEntryAlerts) {
        this.capzoneEntryAlerts = capzoneEntryAlerts;
    }

    /**
     * Checks if this faction user if showing the claim map.
     *
     * @return true if faction user is showing claim map
     */
    public boolean isShowClaimMap() {
        return this.showClaimMap;
    }

    /**
     * Sets if this faction user if showing the claim map.
     *
     * @param showClaimMap if faction user should show claim map
     */
    public void setShowClaimMap(boolean showClaimMap) {
        this.showClaimMap = showClaimMap;
    }

    public int getKills() {
        return this.kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public Deathban getDeathban() {
        return deathban;
    }

    public void setDeathban(Deathban deathban) {
        this.deathban = deathban;
    }

    public void removeDeathban() {
        this.deathban = null;
    }

    public long getLastFactionLeaveMillis() {
        return this.lastFactionLeaveMillis;
    }

    public void setLastFactionLeaveMillis(long lastFactionLeaveMillis) {
        this.lastFactionLeaveMillis = lastFactionLeaveMillis;
    }

    public boolean isShowLightning() {
        return this.showLightning;
    }

    public void setShowLightning(boolean showLightning) {
        this.showLightning = showLightning;
    }

    public Set<UUID> getFactionChatSpying() {
        return this.factionChatSpying;
    }

    public Set<String> getShownScoreboardScores() {
        return shownScoreboardScores;
    }

    public UUID getUserUUID() {
        return userUUID;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(userUUID);
    }
}
