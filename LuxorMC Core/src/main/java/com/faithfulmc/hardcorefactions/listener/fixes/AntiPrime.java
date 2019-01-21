package com.faithfulmc.hardcorefactions.listener.fixes;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class AntiPrime implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onCreeperExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof Creeper) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(ExplosionPrimeEvent e) {
        if (e.getEntity() instanceof Creeper) {
            e.setCancelled(true);
        }
    }
}
