package org.ipvp.hcf.listener;

import com.google.common.base.Preconditions;
import net.minecraft.server.v1_7_R4.EntityLiving;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.ipvp.hcf.HCF;

/**
 * Listener that customises the death-messages to show kills besides name.
 */
public class DeathMessageListener implements Listener {

    private final HCF plugin;

    public DeathMessageListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String message = event.getDeathMessage();
        if (message == null || message.isEmpty()) return;
        event.setDeathMessage(getDeathMessage(message, event.getEntity(), getKiller(event)));
    }

    /**
     * Gets the final killer from the death event including LivingEntity types
     *
     * @param event the event to get from
     * @return the killer from the event
     */
    private CraftEntity getKiller(PlayerDeathEvent event) {
        EntityLiving lastAttacker = ((CraftPlayer) event.getEntity()).getHandle().aX();
        return lastAttacker == null ? null : lastAttacker.getBukkitEntity();
    }

    /**
     * Builds a death message with a given input string, entity and killer.
     *
     * @param input  the original death message
     * @param entity the entity that has been killed
     * @param killer the killer of the entity
     * @return the final death message string
     */
    private String getDeathMessage(String input, Entity entity, Entity killer) {
        // Fancify the message.
        input = input.replaceFirst("\\[", ChatColor.GOLD + "[" + ChatColor.WHITE);
        input = replaceLast(input, "]", ChatColor.GOLD + "]" + ChatColor.WHITE);

        if (entity != null) {
            input = input.replaceFirst("(?i)" + getEntityName(entity), ChatColor.RED + getDisplayName(entity) + ChatColor.YELLOW);
        }

        if (killer != null && (entity == null || !killer.equals(entity))) {
            input = input.replaceFirst("(?i)" + getEntityName(killer), ChatColor.RED + getDisplayName(killer) + ChatColor.YELLOW);
        }

        return input;
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ')', replacement);
    }

    /**
     * Gets the name of a {@link Entity} or username of a {@link Player}.
     *
     * @param entity the {@link Entity} to get for
     * @return username if is a {@link Player}, otherwise type name
     */
    private String getEntityName(Entity entity) {
        Preconditions.checkNotNull(entity, "Entity cannot be null");
        return entity instanceof Player ? ((Player) entity).getName() : ((CraftEntity) entity).getHandle().getName();
    }

    /**
     * Gets the new name of the entity to show the daily kills.
     *
     * @param entity the entity
     * @return entity type name if !instanceof player
     */
    private String getDisplayName(Entity entity) {
        Preconditions.checkNotNull(entity, "Entity cannot be null");
        if (entity instanceof Player) {
            Player player = (Player) entity;
            return player.getName() + ChatColor.GOLD + '[' + ChatColor.WHITE + plugin.getUserManager().getUser(player.getUniqueId()).getKills() + ChatColor.GOLD + ']';
        } else {
            return WordUtils.capitalizeFully(entity.getType().name().replace('_', ' '));
        }
    }
}
