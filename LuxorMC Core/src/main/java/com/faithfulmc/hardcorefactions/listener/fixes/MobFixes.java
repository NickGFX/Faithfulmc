package com.faithfulmc.hardcorefactions.listener.fixes;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MobFixes implements Listener {
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Horse || (e.getEntity() instanceof Skeleton && ((Skeleton) e.getEntity()).getSkeletonType() == Skeleton.SkeletonType.WITHER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEndermanDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Enderman || event.getDamager() instanceof MagmaCube || event.getDamager() instanceof Slime) {
            event.setCancelled(true);
        }
    }
}
