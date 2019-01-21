package com.faithfulmc.hardcorefactions.kit.event;

import com.faithfulmc.hardcorefactions.kit.Kit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KitCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers;

    public static HandlerList getHandlerList() {
        return KitCreateEvent.handlers;
    }

    static {
        handlers = new HandlerList();
    }

    private final Kit kit;
    private boolean cancelled;

    public KitCreateEvent(final Kit kit) {
        this.cancelled = false;
        this.kit = kit;
    }

    public Kit getKit() {
        return this.kit;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return KitCreateEvent.handlers;
    }
}
