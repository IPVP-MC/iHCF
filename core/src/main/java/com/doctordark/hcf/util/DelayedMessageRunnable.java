package com.doctordark.hcf.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runnable used to message {@link Player}s with a delay, can be useful for the {@link org.bukkit.event.player.PlayerLoginEvent}
 * for example, where chat input is not received.
 */
public class DelayedMessageRunnable extends BukkitRunnable {

    private final Player player;
    private final String message;

    public DelayedMessageRunnable(JavaPlugin plugin, Player player, String message) {
        this.player = player;
        this.message = message;

        runTask(plugin);
    }

    @Override
    public void run() {
        player.sendMessage(message);
    }
}