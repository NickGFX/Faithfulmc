package com.faithfulmc.framework.buycraft.api.to;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParseException;

import java.util.Map;

public class PaymentPackage {
    private int id;
    private Map<String, Object> options;

    public PaymentPackage(int id, Map<String, Object> options) {
        this.id = id;
        this.options = options;
    }

    public int getId() {
        return id;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public Double getPrice(){
        return (Double) options.get("price");
    }

    public Integer getServer(){
        return (Integer) options.get("server");
    }

    public Integer getGlobal(){
        return (Integer) options.get("global");
    }

    public JsonObject toJSON(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        JsonObject paymentOptionsObject = new JsonObject();
        for(Map.Entry<String, Object> entry: options.entrySet()){
            String property = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof Number){
                paymentOptionsObject.addProperty(property, (Number) value);
            }
            else if(value instanceof Boolean){
                paymentOptionsObject.addProperty(property, (Boolean) value);
            }
            else if(value instanceof String){
                paymentOptionsObject.addProperty(property, (String) value);
            }
            else{
                throw new JsonParseException("Failed to serialize \'" + property + "\':" + "\'" + value.toString() + "\'");
            }
        }
        jsonObject.add("options", paymentOptionsObject);
        return jsonObject;
    }
}
