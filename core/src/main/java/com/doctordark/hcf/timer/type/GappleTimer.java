package com.doctordark.hcf.timer.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.PlayerTimer;
import com.doctordark.hcf.util.DurationFormatter;
import com.doctordark.util.imagemessage.ImageChar;
import com.doctordark.util.imagemessage.ImageMessage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Timer used to prevent {@link Player}s from using Notch Apples too often.
 */
public class GappleTimer extends PlayerTimer implements Listener {

    private ImageMessage goppleArtMessage;

    public GappleTimer(HCF plugin) {
        super("Gapple", TimeUnit.HOURS.toMillis(6L));

        if (plugin.getImageFolder().getGopple() != null) {
            goppleArtMessage = ImageMessage.newInstance(plugin.getImageFolder().getGopple(), 8, ImageChar.BLOCK.getChar()).appendText("", "",
                    ChatColor.GOLD.toString() + ChatColor.BOLD + ' ' + name + ':',
                    ChatColor.GRAY + "  Consumed",
                    ChatColor.GOLD + " Cooldown Remaining:",
                    ChatColor.GRAY + "  " + DurationFormatUtils.formatDurationWords(defaultCooldown, true, true)
            );
        }
    }

    @Override
    public String getScoreboardPrefix() {
        return ChatColor.YELLOW.toString() + ChatColor.BOLD;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack stack = event.getItem();
        if (stack != null && stack.getType() == Material.GOLDEN_APPLE && stack.getDurability() == 1) {
            Player player = event.getPlayer();
            if (setCooldown(player, player.getUniqueId(), defaultCooldown, false, new Predicate<Long>() {
                @Override
                public boolean test(@Nullable Long value) {
                    return false;
                }
            })) {

                if (goppleArtMessage != null) {
                    goppleArtMessage.sendToPlayer(player);
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Consumed " + ChatColor.GOLD + "Golden Apple" + ChatColor.YELLOW + ", now on a cooldown for " +
                            DurationFormatUtils.formatDurationWords(defaultCooldown, true, true));
                }
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You still have a " + getDisplayName() + ChatColor.RED + " cooldown for another " +
                        ChatColor.BOLD + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.RED + '.');
            }
        }
    }
}
