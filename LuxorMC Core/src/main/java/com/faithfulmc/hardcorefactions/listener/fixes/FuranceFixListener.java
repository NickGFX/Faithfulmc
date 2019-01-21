package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FuranceFixListener implements Listener {
    private final HCF hcf;

    public FuranceFixListener(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerFurnaceSteal(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            Block block = e.getClickedBlock();
            Player clicked = e.getPlayer();
            if (clicked.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            Faction at = hcf.getFactionManager().getFactionAt(block.getLocation());
            if (at != null && at instanceof PlayerFaction) {
                PlayerFaction playerFaction = (PlayerFaction) at;
                if (!playerFaction.getMembers().containsKey(clicked.getUniqueId()) && !playerFaction.isRaidable()) {
                    e.setUseItemInHand(Event.Result.ALLOW);
                    e.setUseInteractedBlock(Event.Result.DENY);
                    if (block.getState() instanceof Sign) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
