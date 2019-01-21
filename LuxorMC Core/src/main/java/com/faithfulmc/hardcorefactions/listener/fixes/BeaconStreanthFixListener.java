package com.faithfulmc.hardcorefactions.listener.fixes;

import net.minecraft.server.v1_7_R4.PacketPlayOutKeepAlive;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.github.paperspigot.event.block.BeaconEffectEvent;

public class BeaconStreanthFixListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionEffectAdd(BeaconEffectEvent event) {
        Player entity = event.getPlayer();
        PotionEffect effect = event.getEffect();
        if ((effect.getAmplifier() >= 1) && (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE))) {
            entity.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(), 0, effect.isAmbient()), true);
            event.setCancelled(true);
        }
        if(effect.getType() == PotionEffectType.SPEED && entity.hasPotionEffect(PotionEffectType.SPEED)){
            event.setCancelled(true);
        }
    }
}
