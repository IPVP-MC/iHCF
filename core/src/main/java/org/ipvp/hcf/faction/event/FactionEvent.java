package org.ipvp.hcf.faction.event;

import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.ipvp.hcf.faction.type.Faction;

/**
 * Represents a faction related event
 */
public abstract class FactionEvent extends Event {

    protected final Faction faction;

    public FactionEvent(final Faction faction) {
        this.faction = Preconditions.checkNotNull(faction, "Faction cannot be null");
    }

    FactionEvent(final Faction faction, boolean async) {
        super(async);
        this.faction = Preconditions.checkNotNull(faction, "Faction cannot be null");
    }

    /**
     * Returns the {@link Faction} involved in this event
     *
     * @return the {@link Faction} that is involved in this event
     */
    public Faction getFaction() {
        return faction;
    }
}