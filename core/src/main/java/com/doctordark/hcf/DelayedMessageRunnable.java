package com.doctordark.hcf;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runnable used to message {@link Player}s with a delay, can be useful for the {@link org.bukkit.event.player.PlayerLoginEvent}
 * for example, where chat input is not received.
 */
public class DelayedMessageRunnable extends BukkitRunnable {

    private final Player player;
    private final String message;

    public DelayedMessageRunnable(Player player, String message) {
        this.player = player;
        this.message = message;
    }

    @Override
    public void run() {
        this.player.sendMessage(this.message);
    }
}