package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EnderpearlRefundListener implements Listener {
    private final HCF hcf;

    public EnderpearlRefundListener(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPearl(PlayerTeleportEvent event) {
        if (event.isCancelled() && event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Player player = event.getPlayer();
            if (hcf.getTimerManager().enderPearlTimer.getRemaining(player) > 0) {
                hcf.getTimerManager().enderPearlTimer.refund(player);
            }
        }
    }
}
