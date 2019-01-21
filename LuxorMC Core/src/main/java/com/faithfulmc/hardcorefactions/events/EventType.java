package com.faithfulmc.hardcorefactions.events;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.tracker.CitadelTracker;
import com.faithfulmc.hardcorefactions.events.tracker.ConquestTracker;
import com.faithfulmc.hardcorefactions.events.tracker.EventTracker;
import com.faithfulmc.hardcorefactions.events.tracker.KothTracker;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;

public enum EventType {
    CONQUEST(
            "Conquest",
            ConquestTracker.class,
            ConfigurationService.GOLD + ChatColor.BOLD.toString() + "CONQUEST " + ChatColor.DARK_GRAY + ConfigurationService.DOUBLEARROW + ConfigurationService.YELLOW.toString() + " "
    ),
    KOTH(
            "KOTH",
            KothTracker.class,
            ConfigurationService.GOLD + ChatColor.BOLD.toString() + "KOTH " + ChatColor.DARK_GRAY + ConfigurationService.DOUBLEARROW + ConfigurationService.YELLOW.toString() + " "
    ),
    CITADEL(
            "Citadel",
            CitadelTracker.class,
            ConfigurationService.GOLD + ChatColor.BOLD.toString() + "CITADEL " + ChatColor.DARK_GRAY + ConfigurationService.DOUBLEARROW + ConfigurationService.YELLOW.toString() + " "
    )
    ;

    private static final ImmutableMap<String, EventType> byDisplayName;

    @Deprecated
    public static EventType getByDisplayName(final String name) {
        return EventType.byDisplayName.get(name.toLowerCase());
    }

    static {
        final ImmutableMap.Builder<String, EventType> builder = new ImmutableBiMap.Builder<>();
        for (final EventType eventType : values()) {
            builder.put(eventType.displayName.toLowerCase(), eventType);
        }
        byDisplayName = builder.build();
    }

    private final Class<? extends EventTracker> eventTrackerClass;
    private EventTracker eventTracker = null;
    private final String displayName;
    private final String prefix;

    private EventType(final String displayName, Class<? extends EventTracker> eventTracker, String prefix) {
        this.displayName = displayName;
        this.eventTrackerClass = eventTracker;
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public EventTracker getEventTracker() {
        if(this.eventTracker == null){
            try {
                this.eventTracker = eventTrackerClass.getConstructor(HCF.class).newInstance(HCF.getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.eventTracker;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}