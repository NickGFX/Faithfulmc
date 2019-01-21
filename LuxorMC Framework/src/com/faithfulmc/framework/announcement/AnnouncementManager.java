package com.faithfulmc.framework.announcement;

import com.faithfulmc.framework.BasePlugin;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public abstract class AnnouncementManager{
    public static final String NO_BROADCAST_META = "NO_BROADCAST";

    protected final BasePlugin plugin;
    protected ConcurrentMap<String, Announcement> announcementConcurrentMap = new ConcurrentHashMap<>();

    public AnnouncementManager(BasePlugin plugin) {
        this.plugin = plugin;
        Map<String, Long> lastUpdate = new HashMap<>();
        long start = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for(Announcement announcement: announcementConcurrentMap.values()){
                if(announcement.getDelay() > 0) {
                    long last = lastUpdate.getOrDefault(announcement.getName(), start);
                    long diff = now - last;
                    long time = TimeUnit.SECONDS.toMillis(announcement.getDelay());
                    if (diff > time) {
                        sendBroadcastMessage(announcement.getLines());
                        lastUpdate.put(announcement.getName(), now);
                    }
                }
            }
        }, 20, 20);
    }

    public void sendBroadcastMessage(String[] message){
        for(Player player: Bukkit.getOnlinePlayers()){
            if(!player.hasMetadata(NO_BROADCAST_META)){
                player.sendMessage(message);
            }
        }
    }

    public abstract List<Announcement> getAllAnnouncemens();

    public List<Announcement> getAnnouncements(){
        return ImmutableList.copyOf(announcementConcurrentMap.values());
    }

    public abstract Announcement getAnnouncement(String name);

    public void removeAnnouncement(Announcement announcement){
        announcementConcurrentMap.remove(announcement.getName());
    }

    public void saveAnnouncement(Announcement announcement){
        announcementConcurrentMap.put(announcement.getName(), announcement);
    }


}
