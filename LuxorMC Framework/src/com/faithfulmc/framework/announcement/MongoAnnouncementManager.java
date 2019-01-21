package com.faithfulmc.framework.announcement;

import com.faithfulmc.framework.BasePlugin;
import com.mongodb.CursorType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;

public class MongoAnnouncementManager extends AnnouncementManager implements Runnable{
    private final MongoCollection<Document> mongoCollection;
    private final MongoCollection<Document> cursorCollection;
    private final UUID serverUID = UUID.randomUUID();
    private Thread announcementThread;

    public MongoAnnouncementManager(BasePlugin plugin) {
        super(plugin);
        MongoDatabase mongoDatabase = plugin.getMongoClient().getDatabase(plugin.getDatabaseName());
        mongoCollection = mongoDatabase.getCollection("announcements");
        cursorCollection = mongoDatabase.getCollection("announcementMessages");

        announcementThread = new Thread(this);

        Bukkit.getScheduler().runTaskLater(plugin, announcementThread::start, 20);
    }

    public void run() {
        for(Document announcementDocument: mongoCollection.find()){
            Announcement announcement = new Announcement(announcementDocument);
            announcementConcurrentMap.put(announcement.getName(), announcement);
        }
        Document query = new Document();
        Document projection = new Document();
        MongoCursor<Document> cursor = cursorCollection.find(query).projection(projection).cursorType(CursorType.TailableAwait).iterator();
        try {
            while (cursor.hasNext()) {//blocking
                Document document = cursor.next();
                UUID serverID = document.get("serverId", UUID.class);
                if (!Objects.equals(serverID, serverUID)) {
                    String announcementName = document.getString("announcement");
                    Document announcementDocument = mongoCollection.find(new Document("name", announcementName)).first();
                    if(announcementDocument != null){
                        Announcement announcement = new Announcement(announcementDocument);
                        announcementConcurrentMap.put(announcementName, announcement);
                    }
                    else{
                        announcementConcurrentMap.remove(announcementName);
                    }
                }
            }
        }
        catch (IllegalStateException ex){
            plugin.getLogger().log(Level.INFO, "Announcment Cursor Thread closing ");
        }
    }

    public void createUpdate(Announcement announcement) {
        Document insert = new Document();
        insert.put("serverId", serverUID);
        insert.put("announcement", announcement.getName());
        cursorCollection.insertOne(insert);
    }

    public Announcement getAnnouncement(String name) {
        Document document = mongoCollection.find(new Document("name", name)).first();
        if(document != null){
            return new Announcement(document);
        }
        return null;
    }

    @Override
    public void saveAnnouncement(Announcement announcement) {
        super.saveAnnouncement(announcement);
        String name = announcement.getName();
        Document document = mongoCollection.find(new Document("name", name)).first();
        if(document == null){
            mongoCollection.insertOne(new Document(announcement.serialize()));
        }
        else {
            mongoCollection.updateOne(Filters.eq("name", name), Filters.and(Updates.set("lines", Arrays.asList(announcement.getLines())), Updates.set("delay", announcement.getDelay())));
        }
        createUpdate(announcement);
    }

    @Override

    public void removeAnnouncement(Announcement announcement) {
        super.removeAnnouncement(announcement);
        String name = announcement.getName();
        mongoCollection.deleteMany(new Document("name", name));
        createUpdate(announcement);
    }

    public List<Announcement> getAllAnnouncemens() {
        List<Announcement> announcements = new ArrayList<>();
        for(Document document: mongoCollection.find()){
            announcements.add(new Announcement(document));
        }
        return announcements;
    }
}
