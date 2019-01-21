package com.faithfulmc.hardcorefactions.timer.event;


import com.faithfulmc.hardcorefactions.timer.Timer;
import com.google.common.base.Optional;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;


public class TimerExpireEvent extends Event {

    private static final HandlerList handlers = new HandlerList();


    public static HandlerList getHandlerList() {

        return handlers;

    }

    private final Optional<UUID> userUUID;
    private final Timer timer;


    public TimerExpireEvent(Timer timer) {

        this.userUUID = Optional.absent();

        this.timer = timer;

    }


    public TimerExpireEvent(UUID userUUID, Timer timer) {

        this.userUUID = Optional.fromNullable(userUUID);

        this.timer = timer;

    }


    public Optional<UUID> getUserUUID() {

        return this.userUUID;

    }


    public Timer getTimer() {

        return this.timer;

    }


    public HandlerList getHandlers() {

        return handlers;

    }

}