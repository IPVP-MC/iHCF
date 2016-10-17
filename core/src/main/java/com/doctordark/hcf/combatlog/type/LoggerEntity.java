package com.doctordark.hcf.combatlog.type;

import com.doctordark.hcf.HCF;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;

import java.util.UUID;

public interface LoggerEntity {

    /**
     * Spawns this NPC.
     *
     * @param plugin the plugin instance
     */
    void postSpawn(HCF plugin);

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
    UUID getUniqueID();

    /**
     * Removes this entity.
     */
    void destroy();
}
