package com.faithfulmc.hardcorefactions.faction.event;


import com.faithfulmc.hardcorefactions.faction.type.Faction;
import org.bukkit.event.Event;


public abstract class FactionEvent extends Event {
    protected final Faction faction;


    public FactionEvent(Faction faction) {
        this.faction = ((Faction) com.google.common.base.Preconditions.checkNotNull(faction, "Faction cannot be null"));

    }

    FactionEvent(Faction faction, boolean async) {
        super(async);
        this.faction = ((Faction) com.google.common.base.Preconditions.checkNotNull(faction, "Faction cannot be null"));

    }


    public Faction getFaction() {
        return this.faction;

    }

}