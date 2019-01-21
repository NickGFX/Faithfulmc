package com.faithfulmc.hardcorefactions.listener.fixes;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class BoatCraftFix implements Listener {
    @EventHandler
    public void onPlayerCraft(CraftItemEvent event){
        if(event.getRecipe().getResult() != null && event.getRecipe().getResult().getType() == Material.BOAT){
            event.setCancelled(true);
        }
    }
}
