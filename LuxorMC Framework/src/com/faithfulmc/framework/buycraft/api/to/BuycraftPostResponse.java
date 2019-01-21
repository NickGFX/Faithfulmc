package com.faithfulmc.framework.buycraft.api.to;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

public class BuycraftPostResponse {
    private Integer error_code;
    private String error_message;

    public BuycraftPostResponse(Integer error_code, String error_message) {
        this.error_code = error_code;
        this.error_message = error_message;
    }

    public BuycraftPostResponse(JsonObject jsonObject){
        if(jsonObject != null){
            error_code = jsonObject.has("error_code") ? jsonObject.get("error_code").getAsInt() : null;
            error_message = jsonObject.has("error_message" ) ? jsonObject.get("error_message").getAsString() : null;
        }
    }

    public boolean isSuccess(){
        return error_code == null || error_code == 203;
    }

    public Integer getError_code() {
        return error_code;
    }

    public String getError_message() {
        return error_message;
    }
}
