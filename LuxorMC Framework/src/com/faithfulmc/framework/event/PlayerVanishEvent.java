package com.faithfulmc.framework.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Collection;

public class PlayerVanishEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers;

    public static HandlerList getHandlerList() {
        return PlayerVanishEvent.handlers;
    }

    static {
        handlers = new HandlerList();
    }

    private final boolean vanished;
    private final Collection viewers;
    private boolean cancelled;

    public PlayerVanishEvent(final Player player, final Collection viewers, final boolean vanished) {
        super(player);
        this.viewers = viewers;
        this.vanished = vanished;
    }

    public Collection getViewers() {
        return this.viewers;
    }

    public boolean isVanished() {
        return this.vanished;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return PlayerVanishEvent.handlers;
    }
}
