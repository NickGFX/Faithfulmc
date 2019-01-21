package com.faithfulmc.hardcorefactions.hcfclass.miner;

import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class MinerLevelEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    private final FactionUser user;
    private final MinerLevel level;

    public MinerLevelEvent(FactionUser user, MinerLevel level) {
        this.user = user;
        this.level = level;
    }

    public FactionUser getUser() {
        return user;
    }

    public MinerLevel getLevel() {
        return level;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}