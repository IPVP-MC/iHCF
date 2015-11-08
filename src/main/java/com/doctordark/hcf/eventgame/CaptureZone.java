package com.doctordark.hcf.eventgame;

import com.doctordark.util.cuboid.Cuboid;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents an area a {@link Player} can use to control.
 */
public class CaptureZone implements ConfigurationSerializable {

    public static final int MINIMUM_SIZE_AREA = 2;

    private String name;

    private String prefix;
    private Cuboid cuboid;
    private Player cappingPlayer;

    private long defaultCaptureMillis;
    private String defaultCaptureWords;
    private long endMillis;

    /**
     * Constructs an {@link CaptureZone} with a given name, {@link Cuboid}, default capture time and an empty prefix.
     *
     * @param name                 the name to construct with
     * @param cuboid               the {@link Cuboid} to construct with
     * @param defaultCaptureMillis the default milliseconds to capture
     */
    public CaptureZone(String name, Cuboid cuboid, long defaultCaptureMillis) {
        this(name, StringUtils.EMPTY, cuboid, defaultCaptureMillis);
    }

    /**
     * Constructs an {@link CaptureZone} with a given name, prefix, {@link Cuboid} and default capture time.
     *
     * @param name                 the name to construct with
     * @param prefix               the prefix to construct with
     * @param cuboid               the {@link Cuboid} to construct with
     * @param defaultCaptureMillis the default milliseconds to capture
     */
    public CaptureZone(String name, String prefix, Cuboid cuboid, long defaultCaptureMillis) {
        this.name = name;
        this.prefix = prefix;
        this.cuboid = cuboid;
        this.setDefaultCaptureMillis(defaultCaptureMillis);
    }

    /**
     * Constructs an {@link CaptureZone} from a map.
     *
     * @param map the map to deserialize from
     */
    public CaptureZone(Map<String, Object> map) {
        this.name = (String) map.get("name");

        Object obj = map.get("prefix");
        if (obj instanceof String) {
            this.prefix = (String) obj;
        }

        obj = map.get("cuboid");
        if (obj instanceof Cuboid) {
            this.cuboid = (Cuboid) obj;
        }

        this.setDefaultCaptureMillis(Long.parseLong((String) map.get("captureMillis")));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", this.name);

        if (this.prefix != null) {
            map.put("prefix", this.prefix);
        }

        if (this.cuboid != null) {
            map.put("cuboid", this.cuboid);
        }

        map.put("captureMillis", Long.toString(this.defaultCaptureMillis));
        return map;
    }

    /**
     * Checks if this {@link CaptureZone} is active.
     *
     * @return true if is currently active
     */
    public boolean isActive() {
        return this.getRemainingCaptureMillis() > 0L;
    }

    /**
     * Gets the name of this {@link CaptureZone}.
     *
     * @return the {@link CaptureZone} name
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the prefix of this {@link CaptureZone}.
     *
     * @return the {@link CaptureZone} prefix
     */
    public String getPrefix() {
        if (this.prefix == null) this.prefix = ""; // safeguard
        return this.prefix;
    }

    /**
     * Gets the display name of this {@link CaptureZone}.
     *
     * @return the {@link CaptureZone} display name
     */
    public String getDisplayName() {
        return this.getPrefix() + this.name;
    }

    /**
     * Gets the {@link Cuboid} of this {@link CaptureZone}.
     *
     * @return the {@link Cuboid} of the {@link CaptureZone}
     */
    public Cuboid getCuboid() {
        return this.cuboid;
    }

    /**
     * Gets the remaining time in milliseconds until this {@link CaptureZone} will be captured.
     *
     * @return the remaining time in milliseconds until captured, or -1 if this is not active
     */
    public long getRemainingCaptureMillis() {
        if (this.endMillis == Long.MIN_VALUE) {
            return -1L;
        }

        if (this.cappingPlayer == null) {
            return this.defaultCaptureMillis;
        }

        return this.endMillis - System.currentTimeMillis();
    }

    /**
     * Sets the remaining time in milliseconds until this {@link CaptureZone} will be captured.
     *
     * @param millis the remaining time in milliseconds until captured
     */
    public void setRemainingCaptureMillis(long millis) {
        this.endMillis = System.currentTimeMillis() + millis;
    }

    /**
     * Gets the default time in milliseconds to capture this {@link CaptureZone}.
     *
     * @return the time in milliseconds
     */
    public long getDefaultCaptureMillis() {
        return this.defaultCaptureMillis;
    }

    public String getDefaultCaptureWords() {
        return this.defaultCaptureWords;
    }

    /**
     * Sets the default time in milliseconds to capture this {@link CaptureZone}.
     *
     * @param millis the milliseconds to set
     */
    public void setDefaultCaptureMillis(long millis) {
        if (this.defaultCaptureMillis != millis) {
            this.defaultCaptureMillis = millis;
            this.defaultCaptureWords = DurationFormatUtils.formatDurationWords(millis, true, true);
        }
    }

    /**
     * Gets the {@link Player} in control of this {@link CaptureZone}.
     *
     * @return the {@link Player} in control
     */
    public Player getCappingPlayer() {
        return this.cappingPlayer;
    }

    /**
     * Sets the {@link Player} in control of this {@link CaptureZone}.
     *
     * @param player the {@link Player} to set
     */
    public void setCappingPlayer(@Nullable Player player) {
        this.cappingPlayer = player;
        if (player == null) {
            this.endMillis = this.defaultCaptureMillis;
        } else {
            this.endMillis = System.currentTimeMillis() + this.defaultCaptureMillis;
        }
    }
}
