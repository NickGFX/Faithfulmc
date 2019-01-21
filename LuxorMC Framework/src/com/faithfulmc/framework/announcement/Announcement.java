package com.faithfulmc.framework.announcement;

import com.faithfulmc.framework.BasePlugin;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Announcement implements ConfigurationSerializable{
    private final String name;
    private String[] lines;
    private int delay;

    public Announcement(String name, String[] lines, int delay) {
        this.name = name;
        this.lines = lines;
        this.delay = delay;
    }

    public Announcement(Map map){
        name = (String) map.get("name");
        List<String> lines = (List<String>) map.get("lines");
        this.lines = lines.toArray(new String[lines.size()]);
        delay = (Integer) map.get("delay");
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("lines", BasePlugin.isMongo() ? Arrays.asList(lines) : lines);
        map.put("delay", delay);
        return map;
    }

    public String getName() {
        return name;
    }

    public String[] getLines() {
        return lines;
    }

    public int getDelay() {
        return delay;
    }

    public void setLines(String[] lines) {
        this.lines = lines;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Announcement that = (Announcement) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
