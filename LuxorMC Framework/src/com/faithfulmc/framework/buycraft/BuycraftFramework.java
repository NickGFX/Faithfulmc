package com.faithfulmc.framework.buycraft;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.buycraft.api.BuycraftException;
import com.faithfulmc.framework.buycraft.api.from.BuycraftCategory;
import com.faithfulmc.framework.buycraft.api.from.BuycraftPayment;
import com.faithfulmc.framework.buycraft.api.to.BuycraftPostResponse;
import com.faithfulmc.framework.buycraft.api.to.ManualPayment;
import com.faithfulmc.util.buycraft.BuycraftAPI;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonArray;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuycraftFramework {
    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR)
            .toFormatter();

    public static UUID getFromCompressed(String string){
        return UUID.fromString (
                string.replaceFirst (
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"
                )
        );
    }

    public static final BuycraftAPI INSTANCE = BasePlugin.getPlugin().getBuycraftAPI();
    public static List<BuycraftCategory> CACHED_CATEGORIES = null;

    public static BuycraftPostResponse addManualPayment(ManualPayment manualPayment) throws BuycraftException {
        JsonObject jsonObject = manualPayment.toJSON();
        JsonElement returnObject;
        try {
            returnObject = INSTANCE.buycraftPOST("payments", jsonObject);
        } catch (IOException exception) {
            throw new BuycraftException(exception);
        }
        return new BuycraftPostResponse(returnObject != null && returnObject.isJsonObject() ? returnObject.getAsJsonObject() : null);
    }

    public static List<BuycraftCategory> getCachedCategories() throws BuycraftException{
        if(CACHED_CATEGORIES == null) {
            CACHED_CATEGORIES = new ArrayList<>();
            JsonElement returnObject;
            try {
                returnObject = INSTANCE.buycraftGET("listing");
            } catch (IOException exception) {
                throw new BuycraftException(exception);
            }
            if (returnObject.isJsonObject()) {
                JsonArray jsonArray = returnObject.getAsJsonObject().getAsJsonArray("categories");
                for(JsonElement jsonElement: jsonArray){
                    CACHED_CATEGORIES.add(new BuycraftCategory(jsonElement.getAsJsonObject()));
                }
                return CACHED_CATEGORIES;
            }
            throw new BuycraftException(returnObject.toString());
        }
        return CACHED_CATEGORIES;
    }

    public static List<BuycraftPayment> getPayments() throws BuycraftException{
        JsonArray returnObject;
        try{
            returnObject = INSTANCE.buycraftGET("payments").getAsJsonArray();
        } catch (IOException exception) {
            throw new BuycraftException(exception);
        }
        List<BuycraftPayment> payments = new ArrayList<>();
        for(JsonElement jsonElement: returnObject){
            payments.add(new BuycraftPayment(jsonElement.getAsJsonObject()));
        }
        return payments;
    }
}
