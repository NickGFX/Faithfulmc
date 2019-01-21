package com.faithfulmc.hardcorefactions.logger;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LoggerSpawnEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final LoggerEntity loggerEntity;

    public LoggerSpawnEvent(LoggerEntity loggerEntity) {
        this.loggerEntity = loggerEntity;
    }

    public LoggerEntity getLoggerEntity() {
        return this.loggerEntity;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
