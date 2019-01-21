package com.faithfulmc.hardcorefactions.listener.fixes;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherFixListener implements Listener {
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if ((e.getWorld().getEnvironment() == World.Environment.NORMAL) && e.toWeatherState()) {
            e.setCancelled(true);
        }
    }
}
