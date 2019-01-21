package com.faithfulmc.hardcorefactions.timer.type;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;


public class CrappleTimer extends PlayerTimer implements org.bukkit.event.Listener {

    public CrappleTimer(JavaPlugin plugin) {
        super("Crapple", TimeUnit.SECONDS.toMillis(8));

    }


    public String getScoreboardPrefix() {

        return ConfigurationService.GOLD.toString() + ChatColor.BOLD;

    }


    @org.bukkit.event.EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {

        ItemStack stack = event.getItem();

        if ((stack != null) && (stack.getType() == org.bukkit.Material.GOLDEN_APPLE) && (stack.getDurability() == 0)) {

            Player player = event.getPlayer();

            if (!setCooldown(player, player.getUniqueId(), this.defaultCooldown, false)) {

                event.setCancelled(true);

                player.sendMessage(ConfigurationService.RED + "You still have a " + getDisplayName() + ConfigurationService.RED + " cooldown for another " + ChatColor.BOLD + HCF.getRemaining(getRemaining(player), true, false) + ConfigurationService.RED + '.');

            }

        }

    }

}