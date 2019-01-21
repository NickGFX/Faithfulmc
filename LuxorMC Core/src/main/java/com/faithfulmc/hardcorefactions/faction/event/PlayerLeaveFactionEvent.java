package com.faithfulmc.hardcorefactions.faction.event;


import com.faithfulmc.hardcorefactions.faction.event.cause.FactionLeaveCause;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.UUID;


public class PlayerLeaveFactionEvent extends FactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();


    public static HandlerList getHandlerList() {

        return handlers;

    }

    private final UUID uniqueID;
    private final FactionLeaveCause cause;
    private boolean cancelled;
    private Optional<Player> player;


    public PlayerLeaveFactionEvent(Player player, PlayerFaction playerFaction, FactionLeaveCause cause) {

        super(playerFaction);

        Preconditions.checkNotNull(player, "Player cannot be null");
                Preconditions.checkNotNull(playerFaction, "Player faction cannot be null");

        Preconditions.checkNotNull("Leave cause cannot be null");

        this.player = Optional.of(player);

        this.uniqueID = player.getUniqueId();

        this.cause = cause;

    }


    public PlayerLeaveFactionEvent(UUID playerUUID, PlayerFaction playerFaction, FactionLeaveCause cause) {

        super(playerFaction);

        Preconditions.checkNotNull(playerUUID, "Player UUID cannot be null");

        Preconditions.checkNotNull(playerFaction, "Player faction cannot be null");

        Preconditions.checkNotNull("Leave cause cannot be null");

        this.uniqueID = playerUUID;

        this.cause = cause;

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


    public FactionLeaveCause getLeaveCause() {

        return this.cause;

    }


    public HandlerList getHandlers() {

        return handlers;

    }


    public boolean isCancelled() {

        return this.cancelled;

    }


    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;

    }

}