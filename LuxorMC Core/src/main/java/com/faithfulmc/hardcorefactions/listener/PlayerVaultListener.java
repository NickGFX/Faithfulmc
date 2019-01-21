package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.vault.VaultManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class PlayerVaultListener implements Listener{
    private final HCF hcf;

    public PlayerVaultListener(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory inventory = event.getInventory();
        if(event.getPlayer() instanceof Player && inventory != null && inventory.getHolder() != null && inventory.getHolder() instanceof VaultManager){
            Player player = (Player) event.getPlayer();
            hcf.getVaultManager().saveVault(player.getUniqueId(), inventory);
        }
    }
}
