package com.faithfulmc.hardcorefactions.events.faction;


import org.mongodb.morphia.annotations.Entity;

import java.util.Map;


@Entity(value = "faction")
public abstract class CapturableFaction extends EventFaction {

    public CapturableFaction(){

    }

    public CapturableFaction(String name) {
        super(name);

    }


    public CapturableFaction(Map<String, Object> map) {
        super(map);

    }

}