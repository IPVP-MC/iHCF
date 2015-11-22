package com.doctordark.hcf.combatlog.type;

import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;

import java.util.UUID;

public interface LoggerEntity {

    /**
     * Gets the Bukkit entity view.
     *
     * @return the {@link org.bukkit.entity.Entity}
     */
    CraftPlayer getBukkitEntity();

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
