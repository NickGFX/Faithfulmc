package com.faithfulmc.util.messgener;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerAssignedEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private final String id;

    public ServerAssignedEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public HandlerList getHandlers(){
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
