package com.doctordark.hcf.eventgame;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.tracker.ConquestTracker;
import com.doctordark.hcf.eventgame.tracker.EventTracker;
import com.doctordark.hcf.eventgame.tracker.KothTracker;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

public enum EventType {

    CONQUEST("Conquest", new ConquestTracker(HCF.getPlugin())), KOTH("KOTH", new KothTracker(HCF.getPlugin()));

    private final EventTracker eventTracker;
    private final String displayName;

    EventType(String displayName, EventTracker eventTracker) {
        this.displayName = displayName;
        this.eventTracker = eventTracker;
    }

    public EventTracker getEventTracker() {
        return eventTracker;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static final ImmutableMap<String, EventType> byDisplayName;

    static {
        ImmutableMap.Builder<String, EventType> builder = new ImmutableBiMap.Builder<>();
        for (EventType eventType : values()) {
            builder.put(eventType.displayName.toLowerCase(), eventType);
        }

        byDisplayName = builder.build();
    }

    @Deprecated
    public static EventType getByDisplayName(String name) {
        return byDisplayName.get(name.toLowerCase());
    }
}
