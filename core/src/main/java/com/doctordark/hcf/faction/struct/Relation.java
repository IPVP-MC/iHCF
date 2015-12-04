package com.doctordark.hcf.faction.struct;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.Faction;
import com.doctordark.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

/**
 * Represents a relation between {@link Faction}s and {@link org.bukkit.entity.Player}s.
 */
public enum Relation {

    MEMBER(3), ALLY(2), ENEMY(1);

    private final int value;

    Relation(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isAtLeast(Relation relation) {
        return this.value >= relation.value;
    }

    public boolean isAtMost(Relation relation) {
        return this.value <= relation.value;
    }

    public boolean isMember() {
        return this == MEMBER;
    }

    public boolean isAlly() {
        return this == ALLY;
    }

    public boolean isEnemy() {
        return this == ENEMY;
    }

    public String getDisplayName() {
        switch (this) {
            case ALLY:
                return toChatColour() + "alliance";
            default:
                return toChatColour() + name().toLowerCase();
        }
    }

    public ChatColor toChatColour() {
        HCF plugin = HCF.getPlugin();
        switch (this) {
            case MEMBER:
                return plugin.getConfiguration().getRelationColourTeammate();
            case ALLY:
                return plugin.getConfiguration().getRelationColourAlly();
            case ENEMY:
            default:
                return plugin.getConfiguration().getRelationColourEnemy();
        }
    }

    public DyeColor toDyeColour() {
        return BukkitUtils.toDyeColor(toChatColour());
    }
}
