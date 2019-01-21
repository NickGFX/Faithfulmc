package com.faithfulmc.hardcorefactions.faction.event;


import com.faithfulmc.hardcorefactions.faction.event.cause.FactionLeaveCause;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.UUID;


public class PlayerLeftFactionEvent extends FactionEvent {

    private static final HandlerList handlers = new HandlerList();


    public static HandlerList getHandlerList() {

        return handlers;

    }

    private final UUID uniqueID;
    private final FactionLeaveCause cause;
    private Optional<Player> player;


    public PlayerLeftFactionEvent(Player player, PlayerFaction playerFaction, FactionLeaveCause cause) {

        super(playerFaction);

        this.player = Optional.of(player);

        this.uniqueID = player.getUniqueId();

        this.cause = cause;

    }


    public PlayerLeftFactionEvent(UUID playerUUID, PlayerFaction playerFaction, FactionLeaveCause cause) {

        super(playerFaction);

        this.uniqueID = playerUUID;

        this.cause = cause;

    }


    public PlayerFaction getFaction() {

        return (PlayerFaction) super.getFaction();

    }


    public Optional<Player> getPlayer() {

        if (this.player == null) {

            this.player = Optional.fromNullable(Bukkit.getPlayer(this.uniqueID));

        }

        return this.player;

    }


    public UUID getUniqueID() {

        return this.uniqueID;

    }


    public FactionLeaveCause getCause() {

        return this.cause;

    }


    public HandlerList getHandlers() {

        return handlers;

    }

}