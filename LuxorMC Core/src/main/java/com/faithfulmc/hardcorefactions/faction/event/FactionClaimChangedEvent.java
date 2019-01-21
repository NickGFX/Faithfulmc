package com.faithfulmc.hardcorefactions.faction.event;


import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.event.cause.ClaimChangeCause;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;


public class FactionClaimChangedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();


    public static HandlerList getHandlerList() {

        return handlers;

    }

    private final CommandSender sender;
    private final ClaimChangeCause cause;
    private final Collection<Claim> affectedClaims;


    public FactionClaimChangedEvent(CommandSender sender, ClaimChangeCause cause, Collection<Claim> affectedClaims) {

        this.sender = sender;

        this.cause = cause;

        this.affectedClaims = affectedClaims;

    }


    public CommandSender getSender() {

        return this.sender;

    }


    public ClaimChangeCause getCause() {

        return this.cause;

    }


    public Collection<Claim> getAffectedClaims() {

        return this.affectedClaims;

    }


    public HandlerList getHandlers() {

        return handlers;

    }

}
