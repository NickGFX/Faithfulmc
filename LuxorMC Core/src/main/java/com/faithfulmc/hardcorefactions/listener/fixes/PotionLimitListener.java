package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class PotionLimitListener implements Listener {
    private static final int EMPTY_BREW_TIME = 400;

    public int getMaxLevel(PotionType type) {
        return ConfigurationService.POTION_LIMITS.getOrDefault(type, type.getMaxLevel());
    }

    public boolean hasExtendedDuration(PotionType potionType) {
        return ConfigurationService.POTION_LENGTH_LIMIT_LONG.getOrDefault(potionType, true);
    }

    public boolean hasShortDuration(PotionType potionType) {
        return ConfigurationService.POTION_LENGTH_LIMIT_SHORT.getOrDefault(potionType, true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBrew(BrewEvent event) {
        if (!testValidity(event.faithful().getResults())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (!testValidity(new ItemStack[]{e.getItem()})) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSplash(PotionSplashEvent e) {
        if (!testValidity(new ItemStack[]{e.getPotion().getItem()})) {
            e.setCancelled(true);
        }
    }


    private boolean testValidity(ItemStack[] contents) {
        for (ItemStack stack : contents) {
            if ((stack != null) && (stack.getType() == Material.POTION) && (stack.getDurability() != 0)) {
                Potion potion = Potion.fromItemStack(stack);
                if (potion != null) {
                    PotionType type = potion.getType();
                    if (type != null) {
                        return potion.getLevel() <= getMaxLevel(type) && (potion.hasExtendedDuration() ? hasExtendedDuration(type) : hasShortDuration(type));
                    }
                }
            }
        }
        return true;
    }
}
