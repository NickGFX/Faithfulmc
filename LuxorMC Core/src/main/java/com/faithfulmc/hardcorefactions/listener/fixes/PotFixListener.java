package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftThrownPotion;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PotFixListener implements Listener{
    private final HCF hcf;

    public PotFixListener(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerThrowPot(ProjectileLaunchEvent event){
        if(Bukkit.getPluginManager().isPluginEnabled("FastPot")){
            return;
        }
        if ((!(event.getEntity() instanceof ThrownPotion)) || (!(event.getEntity().getShooter() instanceof Player))) {
            return;
        }
        Player player = (Player)event.getEntity().getShooter();
        ThrownPotion potion = (ThrownPotion)event.getEntity();
        if ((!player.isDead()) && player.isSprinting() && (((CraftThrownPotion)potion).getHandle().motY < 0.4)) {
            for (PotionEffect potionEffect: potion.getEffects())
            {
                if (potionEffect.getType().equals(PotionEffectType.HEAL)){
                    new BukkitRunnable(){
                        public void run() {
                            if(potion.isValid() && !player.isDead()) {
                                potion.trigger();
                            }
                        }
                    }.runTaskLater(hcf, player.isOnGround() ? 3 : 5);
                    break;
                }
            }
        }
    }
}
