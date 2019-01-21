package com.faithfulmc.hardcorefactions.faction.event;


import com.faithfulmc.hardcorefactions.faction.type.Faction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerClaimEnterEvent extends Event implements Cancellable
        {
            private static final HandlerList handlers=new HandlerList();


public static HandlerList getHandlerList(){
        return handlers;

        }

private final Player player;
private final Faction fromFaction;
private final Faction toFaction;
private final Location from;
private final Location to;
private final EnterCause enterCause;
private boolean cancelled;


public PlayerClaimEnterEvent(Player player,Location from,Location to,Faction fromFaction,Faction toFaction,EnterCause enterCause)
        {

        this.player=player;

        this.from=from;

        this.to=to;

        this.fromFaction=fromFaction;

        this.toFaction=toFaction;

        this.enterCause=enterCause;

        }


public Faction getFromFaction(){

        return this.fromFaction;

        }


public Faction getToFaction(){

        return this.toFaction;

        }


public Player getPlayer(){

        return this.player;

        }


public Location getFrom(){
                return this.from;

        }


public Location getTo(){

        return this.to;

        }


public EnterCause getEnterCause(){

        return this.enterCause;

        }


public boolean isCancelled(){
                return this.cancelled;

        }


public void setCancelled(boolean cancelled){

        this.cancelled=cancelled;

        }


public HandlerList getHandlers(){

        return handlers;

        }



public static enum EnterCause {
    TELEPORT, MOVEMENT;
}
        }