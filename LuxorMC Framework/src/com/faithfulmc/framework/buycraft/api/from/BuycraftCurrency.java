package com.faithfulmc.framework.buycraft.api.from;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;


public class BuycraftCurrency {
    private String iso_4217;
    private String symbol;

    public BuycraftCurrency(String iso_4217, String symbol) {
        this.iso_4217 = iso_4217;
        this.symbol = symbol;
    }

    public BuycraftCurrency(JsonObject jsonObject){
        iso_4217 = jsonObject.get("iso_4217").getAsString();
        symbol = jsonObject.get("symbol").getAsString();
    }

    public String getIso_4217() {
        return iso_4217;
    }

    public String getSymbol() {
        return symbol;
    }
}
