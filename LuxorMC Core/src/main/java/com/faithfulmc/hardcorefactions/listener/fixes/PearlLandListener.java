package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.HCF;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PearlLandListener implements Listener{
    public Map pearlMap = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build().asMap();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        EnderPearl pearl;
        if((pearl = (EnderPearl) pearlMap.remove(player.getUniqueId())) != null){
            pearl.remove();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        if(player.hasMetadata("signClick")){
            player.removeMetadata("signClick", HCF.getInstance());
            return;
        }
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            EnderPearl pearl;
            if ((pearl = (EnderPearl) pearlMap.remove(player.getUniqueId())) != null) {
                pearl.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPearl(ProjectileLaunchEvent event){
        Projectile projectile = event.getEntity();
        ProjectileSource shooter = projectile.getShooter();
        if(shooter instanceof Player && projectile instanceof EnderPearl){
            Player player = (Player) shooter;
            EnderPearl enderPearl = (EnderPearl) projectile;
            pearlMap.put(player.getUniqueId(), enderPearl);
        }
    }
}
