package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
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

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Listener that customises the death-messages to show kills besides name.
 */
public class DeathMessageListener implements Listener {

    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_", Pattern.LITERAL);
    private static final Pattern LEFT_BRACKET_PATTERN = Pattern.compile("\\[");
    private static final Pattern RIGHT_BRACKET_LAST_OCCURRENCE_PATTERN = Pattern.compile("(?s)(.*)\\]");

    private final HCF plugin;

    public DeathMessageListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String message = event.getDeathMessage();
        if (message != null && !message.isEmpty()) {
            Entity entity = event.getEntity();
            Entity killer = getKiller(event);

            // If the death message shows a death by item, replace the brackets in that text with coloured ones.
            // Only the first and last occurrences to prevent people using those characters in item
            // names causing the message to be oddly coloured.
            message = LEFT_BRACKET_PATTERN.matcher(message).replaceFirst(ChatColor.GOLD + "[" + ChatColor.WHITE);
            message = RIGHT_BRACKET_LAST_OCCURRENCE_PATTERN.matcher(message).replaceFirst("$1" + ChatColor.GOLD + "]" + ChatColor.WHITE);

            // Format the killed entity's name
            if (entity != null) {
                message = message.replaceFirst(getEntityName(entity), ChatColor.RED + getFormattedName(entity) + ChatColor.YELLOW);
            }

            // Format the killing entity's name
            if (killer != null && !Objects.equals(killer, entity)) {
                message = message.replaceFirst(getEntityName(killer), ChatColor.RED + getFormattedName(killer) + ChatColor.YELLOW);
            }

            // Finally update with the formatted message.
            event.setDeathMessage(message);
        }
    }

    /**
     * Gets the final killer or damager from the death event
     * including LivingEntity types
     *
     * @param event the event to get from
     * @return the killer from the event
     */
    private CraftEntity getKiller(PlayerDeathEvent event) {
        EntityLiving lastAttacker = ((CraftPlayer) event.getEntity()).getHandle().aX();
        return lastAttacker != null ? lastAttacker.getBukkitEntity() : null;
    }

    /**
     * Gets the name an entity will display in a vanilla death message.
     *
     * @param entity the {@link Entity} to get for
     * @return the death message entity name
     */
    private String getEntityName(Entity entity) {
        return ((CraftEntity) entity).getHandle().getScoreboardDisplayName().c();
    }

    /**
     * Gets the new name of the entity to show the daily kills.
     *
     * @param entity the entity
     * @return entity type name if !instanceof player
     */
    private String getFormattedName(Entity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        if (entity instanceof Player) {
            Player player = (Player) entity;
            return player.getName() + ChatColor.GOLD + '[' + ChatColor.WHITE + plugin.getUserManager().getUser(player.getUniqueId()).getKills() + ChatColor.GOLD + ']';
        } else {
            return UNDERSCORE_PATTERN.matcher(WordUtils.capitalizeFully(entity.getType().name())).replaceAll(" ");
        }
    }
}
