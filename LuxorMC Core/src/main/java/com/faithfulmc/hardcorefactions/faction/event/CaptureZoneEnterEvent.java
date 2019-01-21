package com.faithfulmc.hardcorefactions.faction.event;


import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.faction.CapturableFaction;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;


public class CaptureZoneEnterEvent extends FactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();


    public static HandlerList getHandlerList() {

        return handlers;

    }

    private final CaptureZone captureZone;
    private final Player player;
    private boolean cancelled;


    public CaptureZoneEnterEvent(Player player, CapturableFaction capturableFaction, CaptureZone captureZone) {

        super(capturableFaction);

        Preconditions.checkNotNull(player, "Player cannot be null");

        Preconditions.checkNotNull(captureZone, "Capture zone cannot be null");

        this.captureZone = captureZone;

        this.player = player;

    }


    public CapturableFaction getFaction() {

        return (CapturableFaction) super.getFaction();

    }


    public CaptureZone getCaptureZone() {

        return this.captureZone;

    }


    public Player getPlayer() {
                return this.player;

    }


    public boolean isCancelled() {

        return this.cancelled;

    }


    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;

    }


    public HandlerList getHandlers() {

        return handlers;

    }

}