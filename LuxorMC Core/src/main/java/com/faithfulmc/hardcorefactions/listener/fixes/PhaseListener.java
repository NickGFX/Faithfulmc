package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.concurrent.TimeUnit;

public class PhaseListener implements Listener {
    long gravityBlock;
    long utilityBlock;

    public PhaseListener() {
        this.gravityBlock = TimeUnit.MINUTES.toMillis(45L);
        this.utilityBlock = TimeUnit.MINUTES.toMillis(45L);
    }

    @EventHandler
    public void onMove(PlayerInteractEvent e) {
        if ((e.getPlayer().getLocation().getBlock() != null) && (e.getPlayer().getLocation().getBlock().getType() == Material.TRAP_DOOR) && (!HCF.getInstance().getFactionManager().getFactionAt(e.getPlayer().getLocation()).equals(HCF.getInstance().getFactionManager().getPlayerFaction(e.getPlayer().getUniqueId())))) {
            e.getPlayer().teleport(e.getPlayer().getLocation().add(0.0D, 1.0D, 0.0D));
        }
    }
}
