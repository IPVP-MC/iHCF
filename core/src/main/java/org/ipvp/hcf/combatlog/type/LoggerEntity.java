package org.ipvp.hcf.combatlog.type;

import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;

import java.util.UUID;

public interface LoggerEntity {

    /**
     * Gets the Bukkit entity view.
     *
     * @return the {@link org.bukkit.entity.Entity}
     */
    CraftLivingEntity getBukkitEntity();

    /**
     * Gets the {@link UUID} of the represented.
     *
     * @return the represented {@link UUID}
     */
    UUID getPlayerUUID();

    /**
     * Removes this entity.
     */
    void destroy();
}
