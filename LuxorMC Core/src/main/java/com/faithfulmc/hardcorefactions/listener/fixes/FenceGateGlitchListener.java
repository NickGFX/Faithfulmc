package com.faithfulmc.hardcorefactions.listener.fixes;


import com.luxormc.event.PlayerKnockbackEvent;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityVelocity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.material.Gate;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class FenceGateGlitchListener implements Listener{
    @EventHandler
    public void onPlayerKnockBack(PlayerKnockbackEvent event){
        Player player = event.getPlayer();
        MinecraftServer.getServer().processQueue.add( () -> {
            Location location = player.getLocation();
            Block block = location.getBlock();
            if (isClosed(block) || isClosed(block.getRelative(BlockFace.UP))) {
                EntityPlayer attackedPlayer = ((CraftPlayer) player).getHandle();
                double victimMotX = attackedPlayer.motX;
                double victimMotY = attackedPlayer.motY;
                double victimMotZ = attackedPlayer.motZ;

                attackedPlayer.motX = 0;
                attackedPlayer.motY = event.getDy();
                attackedPlayer.motZ = 0;

                attackedPlayer.playerConnection.sendPacket(new PacketPlayOutEntityVelocity(attackedPlayer));

                attackedPlayer.velocityChanged = false;
                attackedPlayer.motX = victimMotX;
                attackedPlayer.motY = victimMotY;
                attackedPlayer.motZ = victimMotZ;
            }
        }
        );
    }

    public boolean isClosed(Block block){
        Material type = block.getType();
        if(type == Material.FENCE_GATE){
            MaterialData materialData = type.getNewData(block.getData());
            if(materialData instanceof Gate){
                return !((Gate) materialData).isOpen();
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerBucketEmptyEvent event){
        if(event.isCancelled()) {
            Block block = event.getBlockClicked().getRelative(event.getBlockFace());
            Player player = event.getPlayer();
            Location location = player.getLocation();
            if(location.getBlockX() == block.getX() && location.getBlockZ() == block.getZ() && Math.abs(location.getBlockY() - block.getY()) <= 1 && !player.isOnGround()){
                player.setVelocity(new Vector(0, -0.5, 0));
            }
        }
    }
}
