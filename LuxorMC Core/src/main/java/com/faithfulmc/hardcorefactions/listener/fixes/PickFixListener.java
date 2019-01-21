package com.faithfulmc.hardcorefactions.listener.fixes;

import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

public class PickFixListener implements Listener{
    private final List<Material> blocks = ImmutableList.of(Material.STONE, Material.COBBLESTONE);

    public PickFixListener() {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerShovel(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Location location = player.getLocation();
        int yDiff;
        if(event.isCancelled() && (yDiff = location.getBlockY() - block.getY()) >= 0 && yDiff <= 1) {
            int blockX = block.getX();
            int blockZ = block.getZ();
            int playerX = location.getBlockX();
            int playerZ = location.getBlockZ();
            boolean contained = blocks.contains(location.getBlock().getType());
            if(Math.abs(blockX - playerX) <= (contained ? 1.5 : 0.5) && Math.abs(blockZ - playerZ) <= (contained ? 1.5 : 0.5)) {
                ItemStack hand = player.getItemInHand();
                if (hand != null) {
                    Material material = hand.getType();
                    if (material == Material.IRON_PICKAXE || material == Material.DIAMOND_PICKAXE) {
                        int level = hand.getEnchantmentLevel(Enchantment.DIG_SPEED);
                        if (material == Material.DIAMOND_PICKAXE) {
                            level += 2;
                        }
                        for(PotionEffect potionEffect: player.getActivePotionEffects()){
                            if(potionEffect.getType() == PotionEffectType.FAST_DIGGING){
                                level += potionEffect.getAmplifier() + 1;
                                break;
                            }
                        }
                        if (level >= 7) {
                            Material blockType = block.getType();
                            if (blocks.contains(blockType)) {
                                Vector playerLocation = new Vector(playerX, block.getY(), playerZ);
                                Vector blockLocation = new Vector(block.getX(), block.getY(), block.getZ());
                                Vector difference = playerLocation.clone().subtract(blockLocation);
                                player.teleport(player.getLocation().add(difference.clone().multiply(0.5)));
                                player.setVelocity(difference.clone().multiply(0.5));
                            }
                        }
                    }
                }
            }
        }
    }
}
