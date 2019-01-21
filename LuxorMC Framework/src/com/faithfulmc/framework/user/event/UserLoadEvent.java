package com.faithfulmc.framework.user.event;

import com.faithfulmc.framework.user.BaseUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class UserLoadEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    private final BaseUser baseUser;
    private final Player player;

    public UserLoadEvent(BaseUser baseUser, Player player) {
        super(true);
        this.baseUser = baseUser;
        this.player = player;
    }

    public BaseUser getBaseUser() {
        return baseUser;
    }

    public Player getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}