package com.faithfulmc.hardcorefactions.timer.type;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.GlobalTimer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.concurrent.TimeUnit;


public class SOTWTimer extends GlobalTimer implements org.bukkit.event.Listener {

    public SOTWTimer() {

        super("SOTW", TimeUnit.HOURS.toMillis(3));

    }


    public void run() {

        long remainingMillis = getRemaining();

        if (remainingMillis > 0L) {
            org.bukkit.Bukkit.broadcastMessage(ConfigurationService.YELLOW + "SOTW will start in " + ConfigurationService.RED + HCF.getRemaining(getRemaining(), true));
        }

    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {

        if (((e.getEntity() instanceof Player)) && (e.getCause() != EntityDamageEvent.DamageCause.VOID) && (getRemaining() > 0L)) {
            e.setCancelled(true);
        }

    }




    @EventHandler(priority = EventPriority.MONITOR)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {

        if (((e.getEntity() instanceof Player)) && (getRemaining() > 0L)) {

            e.setCancelled(true);

            e.setFoodLevel(20);

        }
    }



    public String getScoreboardPrefix() {

        return ConfigurationService.YELLOW.toString() + ChatColor.BOLD;

    }

}