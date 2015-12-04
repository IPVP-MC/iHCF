package com.doctordark.hcf.scoreboard;

import com.doctordark.hcf.Configuration;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.faction.type.PlayerFaction;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerBoard {

    @Getter
    private boolean sidebarVisible = false;

    private SidebarProvider defaultProvider;
    private SidebarProvider temporaryProvider;
    private BukkitRunnable runnable;

    private final AtomicBoolean removed = new AtomicBoolean(false);
    private final Team members;
    private final Team neutrals;
    private final Team allies;

    private final BufferedObjective bufferedObjective;

    @Getter
    private final Scoreboard scoreboard;

    @Getter
    private final Player player;

    private final HCF plugin;

    public PlayerBoard(HCF plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        Configuration configuration = plugin.getConfiguration();

        this.scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        this.bufferedObjective = new BufferedObjective(scoreboard, configuration.getScoreboardSidebarTitle());

        this.members = scoreboard.registerNewTeam("members");
        this.members.setPrefix(configuration.getRelationColourTeammate().toString());
        this.members.setCanSeeFriendlyInvisibles(true);

        this.neutrals = scoreboard.registerNewTeam("neutrals");
        this.neutrals.setPrefix(configuration.getRelationColourEnemy().toString());

        this.allies = scoreboard.registerNewTeam("allies");
        this.allies.setPrefix(configuration.getRelationColourAlly().toString());

        player.setScoreboard(this.scoreboard);
    }

    /**
     * Removes this {@link PlayerBoard}.
     */
    public void remove() {
        if (!this.removed.getAndSet(true) && this.scoreboard != null) {
            for (Team team : this.scoreboard.getTeams()) {
                team.unregister();
            }

            for (Objective objective : this.scoreboard.getObjectives()) {
                objective.unregister();
            }
        }
    }

    public void setSidebarVisible(boolean visible) {
        this.sidebarVisible = visible;
        this.bufferedObjective.setDisplaySlot(visible ? DisplaySlot.SIDEBAR : null);
    }

    public void setDefaultSidebar(final SidebarProvider provider, long updateInterval) {
        if (provider != this.defaultProvider) {
            this.defaultProvider = provider;
            if (this.runnable != null) {
                this.runnable.cancel();
            }

            if (provider == null) {
                this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
                return;
            }

            (this.runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (removed.get()) {
                        cancel();
                        return;
                    }

                    if (provider == defaultProvider) {
                        updateObjective();
                    }
                }
            }).runTaskTimerAsynchronously(plugin, updateInterval, updateInterval);
        }
    }

    public void setTemporarySidebar(final SidebarProvider provider, final long expiration) {
        if (this.removed.get()) {
            throw new IllegalStateException("Cannot update whilst board is removed");
        }

        this.temporaryProvider = provider;
        this.updateObjective();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (removed.get()) {
                    cancel();
                    return;
                }

                if (temporaryProvider == provider) {
                    temporaryProvider = null;
                    updateObjective();
                }
            }
        }.runTaskLaterAsynchronously(plugin, expiration);
    }

    private void updateObjective() {
        if (this.removed.get()) {
            throw new IllegalStateException("Cannot update whilst board is removed");
        }

        SidebarProvider provider = this.temporaryProvider != null ? this.temporaryProvider : this.defaultProvider;
        if (provider == null) {
            this.bufferedObjective.setVisible(false);
        } else {
            this.bufferedObjective.setAllLines(provider.getLines(player));
            this.bufferedObjective.flip();
        }
    }

    public void addUpdate(Player target) {
        this.addUpdates(Collections.singleton(target));
    }

    public void addUpdates(Iterable<? extends Player> updates) {
        if (this.removed.get()) {
            throw new IllegalStateException("Cannot update whilst board is removed");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (removed.get()) {
                    cancel();
                    return;
                }

                // Lazy load - don't lookup this in every iteration
                PlayerFaction playerFaction = null;
                boolean firstExecute = false;
                //

                for (Player update : updates) {
                    if (player.equals(update)) {
                        members.addPlayer(update);
                        continue;
                    }

                    if (!firstExecute) {
                        playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                        firstExecute = true;
                    }

                    // Lazy loading for performance increase.
                    PlayerFaction targetFaction;
                    if (playerFaction == null || (targetFaction = plugin.getFactionManager().getPlayerFaction(update)) == null) {
                        neutrals.addPlayer(update);
                    } else if (playerFaction == targetFaction) {
                        members.addPlayer(update);
                    } else if (playerFaction.getAllied().contains(targetFaction.getUniqueID())) {
                        allies.addPlayer(update);
                    } else {
                        neutrals.addPlayer(update);
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
