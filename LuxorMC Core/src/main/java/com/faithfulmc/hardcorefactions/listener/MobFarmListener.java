package com.faithfulmc.hardcorefactions.listener;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class MobFarmListener implements Listener{

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event){
        LivingEntity entity = event.getEntity();
        if(entity instanceof Animals || entity instanceof Creature) {
            if (entity.getKiller() != null) {
                Player killer = entity.getKiller();
                killer.giveExp((int) Math.round(event.getDroppedExp() * ExpMultiplierListener.DEFAULT_MULTIPLER));
                if (!killer.getInventory().addItem(event.getDrops().toArray(new ItemStack[event.getDrops().size()])).isEmpty()) {
                    killer.sendMessage(ChatColor.RED + "Your inventory is full.");
                }
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }
}
