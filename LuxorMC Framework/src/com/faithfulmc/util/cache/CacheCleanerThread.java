package com.faithfulmc.util.cache;

import com.google.common.cache.Cache;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class CacheCleanerThread extends Thread{
    private final long time;
    private Plugin plugin;
    private Cache cache;

    public CacheCleanerThread(long time, Plugin plugin, Cache cache) {
        this.time = time;
        this.plugin = plugin;
        this.cache = cache;
    }

    public CacheCleanerThread(Plugin plugin, Cache cache){
        this(5000, plugin, cache);
    }

    public void run(){
        do{
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                plugin.getLogger().log(Level.SEVERE, "Interrupted ", e);
                break;
            }
            cache.cleanUp();
        }
        while(plugin.isEnabled());
    }
}
