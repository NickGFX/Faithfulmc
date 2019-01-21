package com.faithfulmc.framework.buycraft.api.from;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

public class BuycraftPackage {
    private int id;
    private int order;
    private String name;
    private double price;
    private BuycraftSale sale;

    public BuycraftPackage(int id, int order, String name, double price, BuycraftSale sale) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.price = price;
        this.sale = sale;
    }

    public BuycraftPackage(JsonObject jsonObject){
        id = jsonObject.get("id").getAsInt();
        order = jsonObject.get("order").getAsInt();
        name = jsonObject.get("name").getAsString();
        price = jsonObject.get("price").getAsDouble();
        sale = new BuycraftSale(jsonObject.get("sale").getAsJsonObject());
    }

    public int getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public BuycraftSale getSale() {
        return sale;
    }
}
