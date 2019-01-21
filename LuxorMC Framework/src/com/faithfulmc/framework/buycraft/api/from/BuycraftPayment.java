package com.faithfulmc.framework.buycraft.api.from;

import com.faithfulmc.framework.buycraft.BuycraftFramework;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class BuycraftPayment {
    private int id;
    private double amount;
    private Date date;
    private BuycraftCurrency currency;
    private BuycraftPlayer player;

    public BuycraftPayment(int id, double amount, Date date, BuycraftCurrency currency, BuycraftPlayer player) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.currency = currency;
        this.player = player;
    }

    public BuycraftPayment(JsonObject jsonObject){
        id = jsonObject.get("id").getAsInt();
        amount = jsonObject.get("amount").getAsDouble();
        String dateString = jsonObject.get("date").getAsString();
        String dateTime = dateString.substring(0, dateString.length() - 9);
        date = Date.from(LocalDateTime.from(BuycraftFramework.DATE_FORMATTER.parse(dateTime)).atZone(ZoneId.of("UTC")).toInstant());
        currency = new BuycraftCurrency(jsonObject.get("currency").getAsJsonObject());
        player = new BuycraftPlayer(jsonObject.get("player").getAsJsonObject());
    }

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public BuycraftCurrency getCurrency() {
        return currency;
    }

    public BuycraftPlayer getPlayer() {
        return player;
    }
}
