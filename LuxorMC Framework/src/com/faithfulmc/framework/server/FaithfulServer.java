package com.faithfulmc.framework.server;

import com.faithfulmc.framework.BasePlugin;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;

public class FaithfulServer {
    private final BasePlugin plugin;
    private final ObjectId objectId = new ObjectId();
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public FaithfulServer(BasePlugin plugin) {
        this.plugin = plugin;
        database = plugin.getMongoClient().getDatabase(plugin.getDatabaseName());
        collection = database.getCollection("servers", Document.class);
        insert();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 10, 10);
    }

    public void insert(){
        Document document = new Document();
        document.put("_id", objectId);
        document.put("online", true);
        document.put("name", ServerSettings.NAME);
        document.put("whitelisted", Bukkit.hasWhitelist());
        document.put("onlinePlayers", Bukkit.getOnlinePlayers().size());
        document.put("maxPlayers", Bukkit.getMaxPlayers());
        collection.deleteMany(new Document("name", ServerSettings.NAME));
        collection.insertOne(document);
    }

    public void update(){
        collection.updateOne(Filters.eq("_id", objectId),
                Filters.and(
                        Updates.set("whitelisted", Bukkit.hasWhitelist()),
                        Updates.set("onlinePlayers", Bukkit.getOnlinePlayers().size()),
                        Updates.set("maxPlayers", Bukkit.getMaxPlayers())
                ));
    }

    public void close(){
        collection.updateOne(Filters.eq("_id", objectId),
                Filters.and(
                        Updates.set("online", false),
                        Updates.set("onlinePlayers", 0),
                        Updates.set("maxPlayers", Bukkit.getMaxPlayers())
                ));
    }
}
