package com.doctordark.hcf.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public interface SidebarProvider {

    /**
     * Gets the lines this provider will show for a {@link Player}.
     *
     * @param player the {@link Player} to get for
     * @return list of lines to show
     */
    List<SidebarEntry> getLines(Player player);
}