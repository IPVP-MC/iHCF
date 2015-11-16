package org.ipvp.hcf.faction.struct;

import org.ipvp.hcf.faction.type.Faction;

/**
 * Represents the {@link Role} of a {@link org.bukkit.entity.Player} in a {@link Faction}.
 */
public enum Role {

    LEADER("Leader", "**"),
    CAPTAIN("Captain", "*"),
    MEMBER("Member", "");

    private final String name;
    private final String astrix;

    Role(String name, String astrix) {
        this.name = name;
        this.astrix = astrix;
    }

    /**
     * Gets the name of this {@link Role}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the astrix of this {@link Role}.
     *
     * @return the astrix
     */
    public String getAstrix() {
        return astrix;
    }
}
