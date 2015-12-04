package com.doctordark.hcf.deathban;

import com.doctordark.util.PersistableLocation;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

public class Deathban implements ConfigurationSerializable {

    @Getter
    private final String reason;

    @Getter
    private final long creationMillis;

    private final long expiryMillis;

    private final PersistableLocation deathPoint;

    @Getter
    private final boolean eotwDeathban;

    public Deathban(String reason, long duration, PersistableLocation deathPoint, boolean eotwDeathban) {
        this.reason = reason;
        this.creationMillis = System.currentTimeMillis();
        this.expiryMillis = this.creationMillis + duration;
        this.deathPoint = deathPoint;
        this.eotwDeathban = eotwDeathban;
    }

    public Deathban(Map<String, Object> map) {
        this.reason = (String) map.get("reason");
        this.creationMillis = Long.parseLong((String) map.get("creationMillis"));
        this.expiryMillis = Long.parseLong((String) map.get("expiryMillis"));

        Object object = map.get("deathPoint");
        this.deathPoint = object instanceof PersistableLocation ? (PersistableLocation) object : null;
        this.eotwDeathban = (Boolean) map.get("eotwDeathban");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reason", this.reason);
        map.put("creationMillis", Long.toString(this.creationMillis));
        map.put("expiryMillis", Long.toString(this.expiryMillis));
        if (this.deathPoint != null) {
            map.put("deathPoint", this.deathPoint);
        }

        map.put("eotwDeathban", this.eotwDeathban);
        return map;
    }

    /**
     * Gets the initial duration of this {@link Deathban} in milliseconds.
     *
     * @return the initial duration
     */
    public long getInitialDuration() {
        return this.expiryMillis - this.creationMillis;
    }

    /**
     * Checks if this {@link Deathban} is active.
     *
     * @return true if is active
     */
    public boolean isActive() {
        return !this.eotwDeathban && this.getRemaining() > 0L;
    }

    /**
     * Gets the remaining time in milliseconds until this {@link Deathban}
     * is no longer active.
     *
     * @return the remaining time until expired
     */
    public long getRemaining() {
        return this.expiryMillis - System.currentTimeMillis();
    }

    /**
     * Gets the {@link Location} where this player died during {@link Deathban}.
     *
     * @return death {@link Location}
     */
    public Location getDeathPoint() {
        return deathPoint == null ? null : deathPoint.getLocation();
    }
}
