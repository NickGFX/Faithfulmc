package com.faithfulmc.hardcorefactions.faction.event;


import com.faithfulmc.hardcorefactions.faction.struct.Raidable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionDtrChangeEvent extends Event implements Cancellable
        {
            private static final HandlerList handlers=new HandlerList();


public static HandlerList getHandlerList(){
        return handlers;

        }

private final DtrUpdateCause cause;
private final Raidable raidable;
private final double originalDtr;
private boolean cancelled;
private double newDtr;


public FactionDtrChangeEvent(DtrUpdateCause cause,Raidable raidable,double originalDtr,double newDtr)
        {

        this.cause=cause;

        this.raidable=raidable;
        this.originalDtr=originalDtr;
        this.newDtr=newDtr;

        }


public DtrUpdateCause getCause(){

        return this.cause;

        }


public Raidable getRaidable(){

        return this.raidable;

        }


public double getOriginalDtr(){

        return this.originalDtr;

        }


public double getNewDtr(){

        return this.newDtr;

        }


public void setNewDtr(double newDtr){
        this.newDtr=newDtr;

        }


public boolean isCancelled(){

        return(this.cancelled)||(Math.abs(this.newDtr-this.originalDtr)==0.0D);

        }


public void setCancelled(boolean cancelled){

        this.cancelled=cancelled;

        }


public HandlerList getHandlers(){

        return handlers;

        }
public static enum DtrUpdateCause {
    REGENERATION,MEMBER_DEATH;


    private DtrUpdateCause() {
    }

}


        }