package com.faithfulmc.hardcorefactions.listener.fixes;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

public class BlockJumpGlitchFixListener implements Listener {
    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            Player player = event.getPlayer();
            if ((player.getGameMode() == GameMode.CREATIVE) || (player.getAllowFlight())) {
                return;
            }
            Location location = player.getLocation();
            Block blockAgainst = event.getBlockAgainst();
            Block blockPlaced = event.getBlockPlaced();
            if(location.getBlockX() == blockAgainst.getX() && location.getBlockZ() == blockAgainst.getZ() && blockAgainst.getY() < location.getBlockY() - 1 && blockPlaced.getY() < location.getBlockY()){
                if (blockPlaced.getType().isSolid() && !(blockPlaced.getState() instanceof Sign)) {
                    Vector vector = player.getVelocity();
                    player.setVelocity(vector.setY(vector.getY() - 0.41999998688697815D));
                }
            }
        }
    }
}
