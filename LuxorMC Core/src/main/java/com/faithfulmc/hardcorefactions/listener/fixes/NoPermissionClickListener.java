package com.faithfulmc.hardcorefactions.listener.fixes;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class NoPermissionClickListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        boolean op = player.isOp();
        player.setOp(false);
        if ((player.getGameMode() == GameMode.CREATIVE) && (!player.hasPermission("base.command.gamemode"))) {
            e.setCancelled(true);
        }
        player.setOp(op);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlaceCreative(BlockBreakEvent event) {
        Player player = event.getPlayer();
        boolean op = player.isOp();
        player.setOp(false);
        if ((player.getGameMode() == GameMode.CREATIVE) && (!player.hasPermission("base.command.gamemode"))) {
            event.setCancelled(true);
        }
        player.setOp(op);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        boolean op = humanEntity.isOp();
        humanEntity.setOp(false);
        if (((humanEntity instanceof Player)) && (!humanEntity.hasPermission("base.command.gamemode"))) {
            event.setCancelled(true);
        }
        humanEntity.setOp(op);
    }
}
