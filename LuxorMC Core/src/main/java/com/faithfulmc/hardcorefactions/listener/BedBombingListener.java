package com.faithfulmc.hardcorefactions.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BedBombingListener implements Listener{
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Action action = event.getAction();
        if(action == Action.RIGHT_CLICK_BLOCK && block.getWorld().getEnvironment() == World.Environment.NETHER && block.getType() == Material.BED_BLOCK){
            block.setTypeIdAndData(0, (byte) 0, true);
            block.getWorld().createExplosion(block.getLocation().add(0.5, 0.5, 0.5), 6.5f, false);
            event.setCancelled(true);
        }
    }

    private static BlockFace[] NEAR = new BlockFace[] {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPlaceBed(BlockPlaceEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        if(block.getWorld().getEnvironment() == World.Environment.NETHER){
            if(event.getItemInHand() != null && event.getItemInHand().getType() == Material.BED){
                boolean nearPortal = false;
                for(BlockFace blockFace: NEAR){
                    Block otherBlock = block.getRelative(blockFace);
                    Material blockType = otherBlock.getType();
                    nearPortal = blockType == Material.PORTAL || blockType == Material.OBSIDIAN;
                    if(nearPortal){
                        break;
                    }
                }
                if(nearPortal) {
                    player.sendMessage(ChatColor.YELLOW + "You may not bedbomb near a portal");
                    event.setCancelled(true);
                }
            }
        }
    }
}
