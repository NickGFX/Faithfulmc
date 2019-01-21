package com.faithfulmc.hardcorefactions.faction.event;


import com.faithfulmc.hardcorefactions.faction.type.Faction;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;


public class FactionRenameEvent extends FactionEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();


    public static HandlerList getHandlerList() {

        return handlers;

    }

    private final CommandSender sender;
    private final String originalName;
    private boolean cancelled;
    private String newName;


    public FactionRenameEvent(Faction faction, CommandSender sender, String originalName, String newName) {

        super(faction);

        this.sender = sender;

        this.originalName = originalName;

        this.newName = newName;

    }


    public CommandSender getSender() {

        return this.sender;

    }


    public String getOriginalName() {

        return this.originalName;

    }


    public String getNewName() {

        return this.newName;

    }


    public void setNewName(String newName) {
                if (!newName.equals(this.newName)) {

            this.newName = newName;

        }

    }


    public boolean isCancelled() {

        return (this.cancelled) || (this.originalName.equals(this.newName));

    }


    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;

    }


    public HandlerList getHandlers() {

        return handlers;

    }

}