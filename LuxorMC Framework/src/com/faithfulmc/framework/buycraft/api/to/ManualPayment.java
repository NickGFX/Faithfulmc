package com.faithfulmc.framework.buycraft.api.to;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonArray;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

import java.util.List;

public class ManualPayment {
    private String ign;
    private double price;
    private List<PaymentPackage> packages;

    public ManualPayment(String ign, double price, List<PaymentPackage> packages) {
        this.ign = ign;
        this.price = price;
        this.packages = packages;
    }

    public String getIgn() {
        return ign;
    }

    public double getPrice() {
        return price;
    }

    public List<PaymentPackage> getPackages() {
        return packages;
    }

    public JsonObject toJSON(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ign", ign);
        jsonObject.addProperty("price", price);
        JsonArray packageArray = new JsonArray();
        for(PaymentPackage paymentPackage: packages){
            packageArray.add(paymentPackage.toJSON());
        }
        jsonObject.add("packages", packageArray);
        return jsonObject;
    }
}
