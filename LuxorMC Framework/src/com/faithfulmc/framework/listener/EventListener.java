package com.faithfulmc.framework.listener;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.event.AnvilRepairEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener{
    private final BasePlugin plugin;

    public EventListener(BasePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClickLowest(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        Inventory inventory = event.getInventory();
        if (inventory instanceof AnvilInventory) {
            InventoryView view = event.getView();
            int rawSlot = event.getRawSlot();
            if (rawSlot == view.convertSlot(rawSlot)) {
                if (rawSlot == 2) {
                    ItemStack item = event.getCurrentItem();
                    if (item != null) {
                        AnvilRepairEvent anvilRepairEvent = new AnvilRepairEvent(humanEntity, (AnvilInventory)inventory, item);
                        Bukkit.getPluginManager().callEvent(anvilRepairEvent);
                        event.setCurrentItem(anvilRepairEvent.getResult());
                        if(anvilRepairEvent.isCancelled()){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

    }
}
