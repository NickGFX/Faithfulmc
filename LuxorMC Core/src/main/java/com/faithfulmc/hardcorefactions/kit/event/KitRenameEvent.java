package com.faithfulmc.hardcorefactions.kit.event;

import com.faithfulmc.hardcorefactions.kit.Kit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KitRenameEvent extends Event implements Cancellable {
    private static final HandlerList handlers;

    public static HandlerList getHandlerList() {
        return KitRenameEvent.handlers;
    }

    static {
        handlers = new HandlerList();
    }

    private final Kit kit;
    private final String oldName;
    private boolean cancelled;
    private String newName;

    public KitRenameEvent(final Kit kit, final String oldName, final String newName) {
        this.cancelled = false;
        this.kit = kit;
        this.oldName = oldName;
        this.newName = newName;
    }

    public Kit getKit() {
        return this.kit;
    }

    public String getOldName() {
        return this.oldName;
    }

    public String getNewName() {
        return this.newName;
    }

    public void setNewName(final String newName) {
        this.newName = newName;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return KitRenameEvent.handlers;
    }
}
