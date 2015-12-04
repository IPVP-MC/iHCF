package com.doctordark.hcf.faction.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.event.FactionRenameEvent;
import com.doctordark.hcf.faction.struct.Relation;
import com.doctordark.util.BukkitUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Faction implements ConfigurationSerializable {

    public static final String FACTIONLESS_PREFIX = "*";

    protected final UUID uniqueID;
    protected String name;

    protected long creationMillis = System.currentTimeMillis();  // the system millis when the faction was created
    public long lastRenameMillis;  // the system millis when the faction was last renamed

    protected double dtrLossMultiplier = 1.0;
    protected double deathbanMultiplier = 1.0;
    protected boolean safezone;

    public Faction(String name) {
        this.uniqueID = UUID.randomUUID();
        this.name = name;
    }

    public Faction(Map<String, Object> map) {
        this.uniqueID = UUID.fromString((String) map.get("uniqueID"));
        this.name = (String) map.get("name");
        this.creationMillis = Long.parseLong((String) map.get("creationMillis"));
        this.lastRenameMillis = Long.parseLong((String) map.get("lastRenameMillis"));
        this.deathbanMultiplier = (Double) map.get("deathbanMultiplier");
        this.safezone = (Boolean) map.get("safezone");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uniqueID", uniqueID.toString());
        map.put("name", name);
        map.put("creationMillis", Long.toString(creationMillis));
        map.put("lastRenameMillis", Long.toString(lastRenameMillis));
        map.put("deathbanMultiplier", deathbanMultiplier);
        map.put("safezone", safezone);
        return map;
    }

    /**
     * Gets the unique ID of this {@link Faction}.
     *
     * @return the {@link UUID}
     */
    public UUID getUniqueID() {
        return uniqueID;
    }

    /**
     * Gets the name of this {@link Faction}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this {@link Faction}.
     *
     * @param name the name to set
     * @return true if the name was successfully set
     */
    public boolean setName(String name) {
        return setName(name, Bukkit.getConsoleSender());
    }

    /**
     * Sets the name of this {@link Faction}.
     *
     * @param name   the name to set
     * @param sender the setting {@link CommandSender}
     * @return true if the name was successfully set
     */
    public boolean setName(String name, CommandSender sender) {
        if (this.name.equals(name)) {
            return false;
        }

        FactionRenameEvent event = new FactionRenameEvent(this, sender, this.name, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        this.lastRenameMillis = System.currentTimeMillis();
        this.name = name;
        return true;
    }

    public Relation getFactionRelation(Faction faction) {
        if (faction instanceof PlayerFaction) {
            PlayerFaction playerFaction = (PlayerFaction) faction;
            if (playerFaction == this) {
                return Relation.MEMBER;
            }

            if (playerFaction.getAllied().contains(uniqueID)) {
                return Relation.ALLY;
            }
        }

        return Relation.ENEMY;
    }

    public Relation getRelation(CommandSender sender) {
        return sender instanceof Player ? getFactionRelation(HCF.getPlugin().getFactionManager().getPlayerFaction((Player) sender)) : Relation.ENEMY;
    }

    /**
     * Gets the display name of this {@link Faction} to a viewing
     * {@link CommandSender}.
     *
     * @param sender the {@link CommandSender} to get for
     * @return the display name for the viewer
     */
    public String getDisplayName(CommandSender sender) {
        return (safezone ? HCF.getPlugin().getConfiguration().getRelationColourAlly() : getRelation(sender).toChatColour()) + name;
    }

    /**
     * Gets the display name of this {@link Faction} to a viewing
     * {@link Faction}.
     *
     * @param other the target {@link Faction} to get for
     * @return the display name for the viewer
     */
    public String getDisplayName(Faction other) {
        return getFactionRelation(other).toChatColour() + name;
    }

    /**
     * Prints details about this {@link Faction} to a {@link CommandSender}.
     *
     * @param sender the sender to print to
     */
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(' ' + getDisplayName(sender));
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    /**
     * Checks if a deathban will be applied when players are killed
     * in the territory of this {@link Faction}.
     *
     * @return true if will deathban
     */
    public boolean isDeathban() {
        return !safezone && deathbanMultiplier > 0.0D;
    }

    /**
     * Sets if a deathban will be applied when players are killed
     * in the territory of this {@link Faction}.
     * <p>Setting to true will set the DTR multiplier to 1.0</p>
     *
     * @param deathban the value to set
     */
    public void setDeathban(boolean deathban) {
        if (deathban != isDeathban()) {
            this.deathbanMultiplier = deathban ? 1.0D : 0.5D;
        }
    }

    /**
     * Gets the deathban multiplier when players are killed in the territory
     * of this {@link Faction}.
     * <p>To disable deathbans completely, set the multiplier to 0</p>
     *
     * @return the deathban multiplier.
     */
    public double getDeathbanMultiplier() {
        return deathbanMultiplier;
    }

    /**
     * Sets the deathban multiplier when players are killed in the territory
     * of this {@link Faction}.
     * <p>To disable deathbans completely, set the multiplier to 0</p>
     *
     * @param deathbanMultiplier the multiplier to set.
     */
    public void setDeathbanMultiplier(double deathbanMultiplier) {
        Preconditions.checkArgument(deathbanMultiplier >= 0, "Deathban multiplier may not be negative");
        this.deathbanMultiplier = deathbanMultiplier;
    }

    public double getDtrLossMultiplier() {
        return dtrLossMultiplier;
    }

    public void setDtrLossMultiplier(double dtrLossMultiplier) {
        this.dtrLossMultiplier = dtrLossMultiplier;
    }

    /**
     * Checks if this {@link Faction} is a safezone protecting {@link Player}s from PVP and PVE.
     *
     * @return true if is safezone
     */
    public boolean isSafezone() {
        return safezone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Faction faction = (Faction) o;

        if (creationMillis != faction.creationMillis) return false;
        if (lastRenameMillis != faction.lastRenameMillis) return false;
        if (Double.compare(faction.dtrLossMultiplier, dtrLossMultiplier) != 0) return false;
        if (Double.compare(faction.deathbanMultiplier, deathbanMultiplier) != 0) return false;
        if (safezone != faction.safezone) return false;
        if (uniqueID != null ? !uniqueID.equals(faction.uniqueID) : faction.uniqueID != null) return false;
        return !(name != null ? !name.equals(faction.name) : faction.name != null);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = uniqueID != null ? uniqueID.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (creationMillis ^ (creationMillis >>> 32));
        result = 31 * result + (int) (lastRenameMillis ^ (lastRenameMillis >>> 32));
        temp = Double.doubleToLongBits(dtrLossMultiplier);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(deathbanMultiplier);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (safezone ? 1 : 0);
        return result;
    }
}
