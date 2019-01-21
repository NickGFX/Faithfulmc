package com.faithfulmc.hardcorefactions.deathban;

import com.faithfulmc.util.PersistableLocation;
import com.google.common.collect.Maps;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Embedded;

import java.util.Map;

@Embedded
public class Deathban implements ConfigurationSerializable {
    private String reason = null;
    private long creationMillis = 0;
    private long expiryMillis = 0;
    private PersistableLocation deathPoint = null;


    public Deathban() {

    }


    public Deathban(String reason, long duration, PersistableLocation deathPoint) {
        this.reason = reason;
        long millis = System.currentTimeMillis();
        this.creationMillis = millis;
        this.expiryMillis = (millis + duration);
        this.deathPoint = deathPoint;
    }


    public Deathban(Map map) {

        this.reason = ((String) map.get("reason"));

        this.creationMillis = Long.parseLong((String) map.get("creationMillis"));

        this.expiryMillis = Long.parseLong((String) map.get("expiryMillis"));

        Object object = map.get("deathPoint");

        if (object != null) {

            this.deathPoint = ((PersistableLocation) object);

        } else {

            this.deathPoint = null;

        }

    }


    public Map<String, Object> serialize() {
                Map<String, Object> map = Maps.newLinkedHashMap();
        map.put("reason", this.reason);

        map.put("creationMillis", Long.toString(this.creationMillis));

        map.put("expiryMillis", Long.toString(this.expiryMillis));

        if (this.deathPoint != null) {

            map.put("deathPoint", this.deathPoint);

        }

        return map;

    }


    public boolean isActive() {

        return getRemaining() > 0L;

    }


    public String getReason() {

        return this.reason;

    }


    public long getCreationMillis() {

        return this.creationMillis;

    }


    public long getExpiryMillis() {

        return this.expiryMillis;

    }


    public long getRemaining() {

        return this.expiryMillis - System.currentTimeMillis();

    }


    public org.bukkit.Location getDeathPoint() {

        return this.deathPoint == null ? null : this.deathPoint.getLocation();

    }

}