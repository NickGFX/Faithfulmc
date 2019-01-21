package com.faithfulmc.hardcorefactions.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EntityLimitListener implements Listener {


    private static final int MAX_SPAWNED_CHUNK_ENTITIES = 30;
    private static final int MAX_ITEM_CHUNK_ENTITIES = 100;
    private static final int MAX_NATURAL_CHUNK_ENTITIES=15;
    private static final int MAX_CHUNK_GENERATED_ENTITIES = 20;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
            return;
        }
        switch (event.getSpawnReason()) {
            case NATURAL:
                if (event.getLocation().getChunk().getEntities().length > MAX_NATURAL_CHUNK_ENTITIES) {
                    event.setCancelled(true);
                }
                break;
            case CHUNK_GEN:
                if (event.getLocation().getChunk().getEntities().length > MAX_CHUNK_GENERATED_ENTITIES) {
                    event.setCancelled(true);
                }
                break;
            case SPAWNER:
                if (event.getLocation().getChunk().getEntities().length > MAX_SPAWNED_CHUNK_ENTITIES) {
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        List<Item> items = new ArrayList<>();
        for (Entity entity : event.getLocation().getChunk().getEntities()) {
            if (entity instanceof Item) {
                items.add((Item) entity);
            }
        }
        while (items.size() > MAX_ITEM_CHUNK_ENTITIES) {
            items.remove(ThreadLocalRandom.current().nextInt(items.size())).remove();
        }
    }
}
