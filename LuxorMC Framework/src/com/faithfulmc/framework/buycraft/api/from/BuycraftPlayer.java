package com.faithfulmc.framework.buycraft.api.from;

import com.faithfulmc.framework.buycraft.BuycraftFramework;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

import java.util.UUID;

public class BuycraftPlayer {
    private int id;
    private String name;
    private UUID uuid;

    public BuycraftPlayer(int id, String name, UUID uuid) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
    }

    public BuycraftPlayer(JsonObject jsonObject){
        id = jsonObject.get("id").getAsInt();
        name = jsonObject.get("name").getAsString();
        uuid = BuycraftFramework.getFromCompressed(jsonObject.get("uuid").getAsString());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }
}
