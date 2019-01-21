package com.faithfulmc.hardcorefactions.timer.event;


import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.Timer;
import com.google.common.base.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.UUID;


public class TimerStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();


    public static HandlerList getHandlerList() {

        return handlers;

    }

    private final Optional<Player> player;
    private final Optional<UUID> userUUID;
    private final Timer timer;
    private final long duration;


    public TimerStartEvent(Timer timer, long duration) {

        this.player = Optional.absent();

        this.userUUID = Optional.absent();

        this.timer = timer;

        this.duration = duration;

    }


    public TimerStartEvent(@Nullable Player player, UUID uniqueId, PlayerTimer timer, long duration) {

        this.player = Optional.fromNullable(player);

        this.userUUID = Optional.fromNullable(uniqueId);

        this.timer = timer;

        this.duration = duration;

    }


    public Optional<Player> getPlayer() {

        return this.player;

    }


    public Optional<UUID> getUserUUID() {

        return this.userUUID;

    }


    public Timer getTimer() {

        return this.timer;

    }


    public long getDuration() {

        return this.duration;

    }


    public HandlerList getHandlers() {

        return handlers;

    }

}