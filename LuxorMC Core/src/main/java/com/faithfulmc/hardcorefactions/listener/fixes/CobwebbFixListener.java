package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class CobwebbFixListener implements Listener {
    private final HCF hcf;
    private Set<Block> todo = new HashSet<>();

    public CobwebbFixListener(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack hand = event.getItemInHand();
        if (hand != null && hand.getType() == Material.WEB) {
            Block block = event.getBlockPlaced();
            todo.add(block);
            new BukkitRunnable() {
                public void run() {
                    todo.remove(block);
                    if (block.getType() == Material.WEB) {
                        block.setType(Material.AIR);
                    }
                }
            }.runTaskLater(hcf, ConfigurationService.KIT_MAP ? 20 * 5 : 60 * 20);
        }
    }

    public void cleanUp() {
        for (Block block : todo) {
            if (block.getType() == Material.WEB) {
                block.setType(Material.AIR);
            }
        }
        todo.clear();
    }
}

