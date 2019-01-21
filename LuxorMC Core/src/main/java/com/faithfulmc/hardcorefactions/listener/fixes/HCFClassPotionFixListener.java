package com.faithfulmc.hardcorefactions.listener.fixes;

import com.luxormc.event.PotionEffectExpiresEvent;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class HCFClassPotionFixListener implements Listener{
    private final HCF plugin;

    public HCFClassPotionFixListener(HCF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionEffectExpire(PotionEffectExpiresEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            HCFClass hcfClass = plugin.getHcfClassManager().getEquippedClass(player);
            if (hcfClass != null) {
                new BukkitRunnable() {
                    public void run() {
                        if(player.isOnline() && hcfClass.isApplicableFor(player)) {
                            for (PotionEffect potionEffect : hcfClass.getPassiveEffects()) {
                                if (!player.hasPotionEffect(potionEffect.getType())) {
                                    player.addPotionEffect(potionEffect, true);
                                }
                            }
                        }
                    }
                }.runTask(HCF.getInstance());
            }
        }
    }
}
