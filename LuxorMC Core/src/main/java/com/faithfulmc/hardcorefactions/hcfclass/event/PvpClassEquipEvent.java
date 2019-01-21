package com.faithfulmc.hardcorefactions.hcfclass.event;

import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PvpClassEquipEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final HCFClass pvpClass;

    public PvpClassEquipEvent(Player player, HCFClass pvpClass) {
        super(player);
        this.pvpClass = pvpClass;
    }

    public HCFClass getPvpClass() {
        return this.pvpClass;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
