package com.faithfulmc.hardcorefactions.listener;

import com.luxormc.event.PlayerMineItemsEvent;
import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class OreMultiplierListener implements Listener{
    public static Set<Material> ORES = Sets.immutableEnumSet(
            Material.DIAMOND,
            Material.GOLD_INGOT,
            Material.IRON_INGOT,
            Material.COAL,
            Material.EMERALD,
            Material.INK_SACK
    );

    public static double MULTIPLIER = 1.25;

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMine(PlayerMineItemsEvent event){
        Player player = event.getPlayer();
        for (ItemStack itemStack : event.getStackCollection()) {
            if (itemStack != null) {
                Material type = itemStack.getType();
                if (ORES.contains(type) ) {
                    int amount = itemStack.getAmount();
                    amount *= MULTIPLIER;
                    if (MULTIPLIER > 1 && ThreadLocalRandom.current().nextDouble() < MULTIPLIER % 1) {
                        amount++;
                    }
                    itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), amount));
                }
            }
        }
    }
}
