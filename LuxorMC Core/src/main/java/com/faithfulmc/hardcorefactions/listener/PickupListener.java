package com.faithfulmc.hardcorefactions.listener;

import com.luxormc.event.PlayerMineItemsEvent;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PickupListener implements Listener {
    private static final List<Material> COBBLE = Arrays.asList(
            Material.COBBLESTONE,
            Material.STONE);
    private static final List<Material> MOBDROPS = Arrays.asList(
            Material.ROTTEN_FLESH,
            Material.SPIDER_EYE,
            Material.STRING,
            Material.POISONOUS_POTATO,
            Material.POTATO,
            Material.BOW,
            Material.BONE,
            Material.ARROW);

    public static final String NO_MOBDROPS_META = "NO_MOBDROPS", NO_COBBLE_META = "NO_COBBLE";

    private final HCF hcf;

    public PickupListener(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        if (item != null){
            ItemStack itemStack = item.getItemStack();
            if(itemStack != null) {
                Material type = itemStack.getType();
                if (COBBLE.contains(type)) {
                    Player player = event.getPlayer();
                    if (player.hasMetadata(NO_COBBLE_META)) {
                        event.setCancelled(true);
                    }
                } else if (MOBDROPS.contains(type)) {
                    Player player = event.getPlayer();
                    if (player.hasMetadata(NO_MOBDROPS_META)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMine(PlayerMineItemsEvent event){
        Player player = event.getPlayer();
        Collection<ItemStack> itemStackCollection = event.getStackCollection();
        if(player.hasMetadata(NO_COBBLE_META)) {
            itemStackCollection.removeIf(stack -> stack != null && COBBLE.contains(stack.getType()));
        }
        if(player.hasMetadata(NO_MOBDROPS_META)) {
            itemStackCollection.removeIf(stack -> stack != null && MOBDROPS.contains(stack.getType()));
        }
        if(itemStackCollection.isEmpty()){
            event.setCancelled(true);
        }
    }
}
