package com.faithfulmc.framework.user.mongo;

import com.faithfulmc.framework.BasePlugin;
import com.mongodb.CursorType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class CursorThread implements Runnable {
    private final BasePlugin basePlugin;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    private final UUID serverUID = UUID.randomUUID();
    private final MongoUserManager mongoUserManager;
    private Thread cursorThread;

    public CursorThread(BasePlugin plugin) {
        this.basePlugin = plugin;
        database = plugin.getMongoClient().getDatabase(plugin.getDatabaseName());
        collection = database.getCollection("Messages");
        mongoUserManager = (MongoUserManager) plugin.getUserManager();

        cursorThread = new Thread(this);
        Bukkit.getScheduler().runTaskLater(plugin, cursorThread::start, 20);
    }

    public void run() {
        Document query = new Document();
        Document projection = new Document();
        MongoCursor<Document> cursor = collection.find(query).projection(projection).cursorType(CursorType.TailableAwait).iterator();
        try {
            while (cursor.hasNext()) {//blocking
                Document document = cursor.next();
                UUID uuid = document.get("id", UUID.class);
                UUID serverID = document.get("serverId", UUID.class);
                if (!Objects.equals(serverID, serverUID)) {
                    mongoUserManager.mongoFetch(uuid);
                }
            }
        }
        catch (IllegalStateException ex){
            basePlugin.getLogger().log(Level.INFO, "Cursor Thread closing ");
        }
    }

    public void createUpdate(UUID user) {
        Document insert = new Document();
        insert.put("id", user);
        insert.put("serverId", serverUID);
        collection.insertOne(insert);
    }
}
