package com.faithfulmc.framework.buycraft.api.from;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

public class BuycraftSale {
    private boolean active;
    private double discount;

    public BuycraftSale(boolean active, double discount) {
        this.active = active;
        this.discount = discount;
    }

    public BuycraftSale(JsonObject jsonObject){
        active = jsonObject.get("active").getAsBoolean();
        discount = jsonObject.get("discount").getAsDouble();
    }

    public boolean isActive() {
        return active;
    }

    public double getDiscount() {
        return discount;
    }
}
