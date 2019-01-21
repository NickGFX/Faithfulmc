package com.faithfulmc.framework.user.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Embedded;

import java.util.LinkedHashMap;
import java.util.Map;

@Embedded
public class NameHistory implements ConfigurationSerializable {
    private String name;
    private long millis;

    public NameHistory(){

    }

    public NameHistory(final Map map) {
        this.name = (String) map.get("name");
        this.millis = Long.parseLong((String) map.get("millis"));
    }

    public NameHistory(final String name, final long millis) {
        this.name = name;
        this.millis = millis;
    }

    public Map serialize() {
        final LinkedHashMap map = new LinkedHashMap();
        map.put("name", this.name);
        map.put("millis", Long.toString(this.millis));
        return map;
    }

    public String getName() {
        return this.name;
    }

    public long getMillis() {
        return this.millis;
    }
}
