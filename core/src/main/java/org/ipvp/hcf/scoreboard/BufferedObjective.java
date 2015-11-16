package org.ipvp.hcf.scoreboard;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BufferedObjective {

    private static final int MAX_SIDEBAR_ENTRIES = 16;
    private static final int MAX_NAME_LENGTH = 16;
    private static final int MAX_PREFIX_LENGTH = 16;
    private static final int MAX_SUFFIX_LENGTH = 16;

    private final Scoreboard scoreboard;

    private Set<String> previousLines = new HashSet<>();
    private TIntObjectHashMap<SidebarEntry> contents = new TIntObjectHashMap<>();

    private boolean requiresUpdate = false; // If the scoreboard needs an update.
    private String title;
    private Objective current;
    private DisplaySlot displaySlot;

    public BufferedObjective(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.title = RandomStringUtils.randomAlphabetic(4);
        this.current = scoreboard.registerNewObjective("buffered", "dummy");
    }

    public void setTitle(String title) {
        if (this.title == null || !this.title.equals(title)) {
            this.title = title;
            this.requiresUpdate = true;
        }
    }

    public void setDisplaySlot(DisplaySlot slot) {
        this.displaySlot = slot;
        this.current.setDisplaySlot(slot);
    }

    public void setAllLines(List<SidebarEntry> lines) {
        if (lines.size() != this.contents.size()) {
            this.contents.clear();
            if (lines.isEmpty()) {
                this.requiresUpdate = true;
                return;
            }
        }

        int size = Math.min(MAX_SIDEBAR_ENTRIES, lines.size());
        int count = 0;
        for (SidebarEntry sidebarEntry : lines) {
            this.setLine(size - count++, sidebarEntry);
        }
    }

    public void setLine(int lineNumber, SidebarEntry sidebarEntry) {
        SidebarEntry value = this.contents.get(lineNumber);
        if (value == null || value != sidebarEntry) {
            this.contents.put(lineNumber, sidebarEntry);
            this.requiresUpdate = true;
        }
    }

    public void flip() {
        if (!this.requiresUpdate) {
            return;
        }

        Set<String> adding = new HashSet<>();
        contents.forEachEntry(new TIntObjectProcedure<SidebarEntry>() {
            @Override
            public boolean execute(int i, SidebarEntry sidebarEntry) {
                String name = sidebarEntry.name;
                if (name.length() > MAX_NAME_LENGTH) name = name.substring(0, MAX_NAME_LENGTH);

                Team team = scoreboard.getOrRegisterNewTeam(name);

                if (sidebarEntry.prefix != null) { // Trim the prefix of this.
                    team.setPrefix(sidebarEntry.prefix.length() > MAX_PREFIX_LENGTH ? sidebarEntry.prefix.substring(0, MAX_PREFIX_LENGTH) : sidebarEntry.prefix);
                }

                if (sidebarEntry.suffix != null) { // Trim the suffix of this.
                    team.setSuffix(sidebarEntry.suffix.length() > MAX_SUFFIX_LENGTH ? sidebarEntry.suffix.substring(0, MAX_SUFFIX_LENGTH) : sidebarEntry.suffix);
                }

                adding.add(name);
                if (!team.hasEntry(name)) {
                    team.addEntry(name);
                }

                current.getScore(name).setScore(i);
                return true;
            }
        });

        // Reset the previous scores.
        this.previousLines.removeAll(adding);
        Iterator<String> iterator = this.previousLines.iterator();
        while (iterator.hasNext()) {
            String last = iterator.next();
            Team team = this.scoreboard.getTeam(last);
            if (team != null) {
                team.removeEntry(last);
            }

            this.scoreboard.resetScores(last);
            iterator.remove();
        }

        this.previousLines = adding; // flip around
        this.current.setDisplayName(this.title);
        this.requiresUpdate = false;
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
