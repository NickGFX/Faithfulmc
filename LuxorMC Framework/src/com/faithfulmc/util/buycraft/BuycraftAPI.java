package com.faithfulmc.util.buycraft;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class BuycraftAPI {
    public static final String ENDPOINT = "https://plugin.buycraft.net";
    private final String secret;

    public BuycraftAPI(String secret) {
        this.secret = secret;
    }

    public JsonElement buycraftGET(String subURL) throws IOException{
        return new HTTPRequest(ENDPOINT  + "/" + subURL)
                .open()
                .requestMethod("GET")
                .setRequestProperty("X-Buycraft-Secret", secret)
                .disconnectAndReturn(self ->
                        new JsonParser().parse(
                                new BufferedReader(new InputStreamReader(self.getHttpURLConnection().getInputStream()))
                        ));
    }

    public JsonElement buycraftPOST(String subURL, JsonObject json) throws IOException{
        String jsonString = json.toString();
        System.out.println(jsonString);
        HTTPRequest request = new HTTPRequest(ENDPOINT + "/" + subURL);
        try {
            request.open()
                    .requestMethod("POST")
                    .setRequestProperty("X-Buycraft-Secret", secret)
                    .setRequestProperty("Content-Length", Integer.toString(jsonString.getBytes().length))
                    .setRequestProperty("Content-Type", "application/json")
                    .doOutput(true)
                    .outputStream(outputStream -> new DataOutputStream(outputStream).writeBytes(jsonString));
            return request.disconnectAndReturn(self ->
                    new JsonParser().parse(
                            new BufferedReader(new InputStreamReader(self.getHttpURLConnection().getInputStream()))
                    ));
        }
        catch (IOException exception){
            return request.disconnectAndReturn(
                    self ->
                        new JsonParser().parse(
                                new BufferedReader(new InputStreamReader(self.getHttpURLConnection().getErrorStream()))
                        )

            );
        }

    }

}
