package com.faithfulmc.hardcorefactions.events;

import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class EventCapture {
    private String name;
    private EventType eventType;
    private String factionname;
    private String player;
    @Embedded
    private List<String> playerNames;

    public EventCapture(){
    }

    public EventCapture(String name, EventType eventType, String factionname, String player, List<String> playerNames) {
        this.name = name;
        this.eventType = eventType;
        this.factionname = factionname;
        this.player = player;
        this.playerNames = playerNames;
    }

    public String getName() {
        return name;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getPlayer() {
        return player;
    }

    public String getFactionname() {
        return factionname;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }
}
