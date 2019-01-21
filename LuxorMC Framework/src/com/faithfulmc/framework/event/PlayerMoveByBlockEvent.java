package com.faithfulmc.framework.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerMoveByBlockEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers;

    public static HandlerList getHandlerList() {
        return PlayerMoveByBlockEvent.handlers;
    }

    static {
        handlers = new HandlerList();
    }

    private Location from;
    private Location to;
    private boolean cancelled;

    public PlayerMoveByBlockEvent(final Player player, final Location to, final Location from) {
        super(player);
        this.from = from;
        this.to = to;
    }

    public Location getFrom() {
        return this.from;
    }

    public void setFrom(final Location from) {
        this.from = from;
    }

    public Location getTo() {
        return this.to;
    }

    public void setTo(final Location to) {
        this.to = to;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return PlayerMoveByBlockEvent.handlers;
    }
}
