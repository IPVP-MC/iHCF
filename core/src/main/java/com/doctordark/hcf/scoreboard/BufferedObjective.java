package com.doctordark.hcf.scoreboard;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BufferedObjective {

    private static final int MAX_SIDEBAR_ENTRIES = 16;
    private static final int MAX_NAME_LENGTH = 16;
    private static final int MAX_PREFIX_LENGTH = 16;
    private static final int MAX_SUFFIX_LENGTH = 16;

    private final Scoreboard scoreboard;
    private final String title;
    private final TIntObjectHashMap<SidebarEntry> contents = new TIntObjectHashMap<>();

    private Set<String> previousLines = new HashSet<>();
    private AtomicBoolean requiresUpdate = new AtomicBoolean(false); // If the scoreboard needs an update.
    private Objective current;
    private DisplaySlot displaySlot;

    public BufferedObjective(Scoreboard scoreboard, String title) {
        this.scoreboard = scoreboard;
        this.title = title;
        this.current = scoreboard.registerNewObjective("buffered", "dummy");
    }

    public void setDisplaySlot(DisplaySlot slot) {
        this.displaySlot = slot;
        this.current.setDisplaySlot(slot);
    }

    public void setAllLines(List<SidebarEntry> lines) {
        synchronized (this.contents) {
            if (lines.size() != this.contents.size()) {
                this.contents.clear();
                if (lines.isEmpty()) {
                    this.requiresUpdate.set(true);
                    return;
                }
            }

            int size = Math.min(MAX_SIDEBAR_ENTRIES, lines.size());
            int count = 0, lineNumber;
            for (SidebarEntry sidebarEntry : lines) {
                lineNumber = size - count++;
                SidebarEntry value = this.contents.get(lineNumber);
                if (value == null || value != sidebarEntry) {
                    this.contents.put(lineNumber, sidebarEntry);
                    this.requiresUpdate.set(true);
                }
            }
        }
    }

    public void flip() {
        if (this.requiresUpdate.getAndSet(false)) {
            Set<String> newLines = new HashSet<>(this.contents.size());
            this.contents.forEachEntry(new TIntObjectProcedure<SidebarEntry>() {
                @Override
                public boolean execute(int i, SidebarEntry sidebarEntry) {
                    String name = sidebarEntry.name.length() > MAX_NAME_LENGTH ? sidebarEntry.name.substring(0, MAX_NAME_LENGTH) : sidebarEntry.name;
                    Team team = scoreboard.getTeam(name);
                    if (team == null ) {
                        team = scoreboard.registerNewTeam(name);
                    }

                    if (sidebarEntry.prefix != null) {
                        team.setPrefix(sidebarEntry.prefix.length() > MAX_PREFIX_LENGTH ? sidebarEntry.prefix.substring(0, MAX_PREFIX_LENGTH) : sidebarEntry.prefix);
                    }

                    if (sidebarEntry.suffix != null) {
                        team.setSuffix(sidebarEntry.suffix.length() > MAX_SUFFIX_LENGTH ? sidebarEntry.suffix.substring(0, MAX_SUFFIX_LENGTH) : sidebarEntry.suffix);
                    }

                    newLines.add(sidebarEntry.name);
                    if (!team.hasEntry(sidebarEntry.name)) {
                        team.addEntry(sidebarEntry.name);
                    }

                    current.getScore(sidebarEntry.name).setScore(i);
                    return true;
                }
            });

            // Reset the previous scores.
            this.previousLines.removeAll(newLines);
            Iterator<String> iterator = this.previousLines.iterator();
            while (iterator.hasNext()) {
                String last = iterator.next();
                Team team = this.scoreboard.getTeam(last);
                if (team != null) {
                    team.removeEntry(last);
                    this.scoreboard.resetScores(last);
                }

                iterator.remove();
            }

            this.previousLines = newLines; // flip around
            this.current.setDisplayName(this.title);
        }
    }

    // Hides the objective from the display slot until flip() is called
    public void setVisible(boolean value) {
        if (this.displaySlot != null && !value) {
            this.scoreboard.clearSlot(this.displaySlot);
            this.displaySlot = null;
        } else if (this.displaySlot == null && value) {
            this.current.setDisplaySlot(this.displaySlot = DisplaySlot.SIDEBAR);
        }
    }
}
