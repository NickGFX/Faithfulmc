package com.faithfulmc.hardcorefactions.visualise;

import com.luxormc.block.BlockPosition;
import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.*;

public class VisualiseHandler {
    private final Table<UUID, BlockPosition, VisualBlock> table = HashBasedTable.create();

    public VisualiseHandler() {
    }

    public void clearAll(Player player, boolean send) {
        table.rowMap().remove(player.getUniqueId());
        ((CraftPlayer) player).getHandle().clearFakeBlocks(send);
    }

    public void clearVisualType(Player player, VisualType visualType, boolean send) {
        clearVisualType(player, visualType, null, send);
    }

    public void clearVisualType(Player player, VisualType visualType, Predicate<VisualBlock> predicate, boolean send){
        List<BlockPosition> removeFromClient = new ArrayList<>();
        synchronized (table){
            Map<BlockPosition, VisualBlock> currentBlocks = table.row(player.getUniqueId());
            for(Map.Entry<BlockPosition, VisualBlock> entry: new ArrayList<>(currentBlocks.entrySet())){
                BlockPosition blockPosition = entry.getKey();
                VisualBlock visualBlock = entry.getValue();
                VisualType blockVisualType = visualBlock.getVisualType();
                if(blockVisualType == visualType && (predicate == null || predicate.apply(visualBlock))){
                    removeFromClient.add(blockPosition);
                    currentBlocks.remove(blockPosition);
                }
            }
        }
        ((CraftPlayer) player).getHandle().setFakeBlocks(Collections.emptyMap(), removeFromClient, send);
    }

    public Map<BlockPosition, MaterialData> addVisualType(Player player, Collection<BlockPosition> locations, VisualType visualType, boolean send){
        Map<BlockPosition, MaterialData> sendToClient = new HashMap<>();
        locations.removeIf(blockPosition -> {
            World world = player.getWorld();
            Block block = world.getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            Material material = block.getType();
            return material.isSolid();
        });
        synchronized (table) {
            Iterator<BlockPosition> iterator = locations.iterator();
            for(VisualBlockData visualBlockData: visualType.blockFiller().bulkGenerate(player, locations)){
                BlockPosition blockPosition = iterator.next();
                sendToClient.put(blockPosition, visualBlockData);
                table.put(player.getUniqueId(), blockPosition, new VisualBlock(visualType, visualBlockData, blockPosition));
            }
        }
        ((CraftPlayer) player).getHandle().setFakeBlocks(sendToClient, Collections.emptyList(), send);
        return sendToClient;
    }

    public Map<BlockPosition, MaterialData> setVisualType(Player player, Collection<BlockPosition> locations, VisualType visualType, boolean send){
        Map<BlockPosition, MaterialData> sendToClient = new HashMap<>();
        List<BlockPosition> removeFromClient = new ArrayList<>();
        locations.removeIf(blockPosition -> {
            World world = player.getWorld();
            Block block = world.getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            Material material = block.getType();
            return material.isSolid();
        });
        synchronized (table) {
            Map<BlockPosition, VisualBlock> currentBlocks = table.row(player.getUniqueId());
            for(Map.Entry<BlockPosition, VisualBlock> entry: new ArrayList<>(currentBlocks.entrySet())){
                BlockPosition blockPosition = entry.getKey();
                VisualBlock visualBlock = entry.getValue();
                VisualType blockVisualType = visualBlock.getVisualType();
                if(blockVisualType == visualType){
                    if(!locations.remove(blockPosition)){
                        removeFromClient.add(blockPosition);
                        currentBlocks.remove(blockPosition);
                    }
                }
            }
            Iterator<BlockPosition> iterator = locations.iterator();
            for(VisualBlockData visualBlockData: visualType.blockFiller().bulkGenerate(player, locations)){
                BlockPosition blockPosition = iterator.next();
                sendToClient.put(blockPosition, visualBlockData);
                table.put(player.getUniqueId(), blockPosition, new VisualBlock(visualType, visualBlockData, blockPosition));
            }
        }
        ((CraftPlayer) player).getHandle().setFakeBlocks(sendToClient, removeFromClient, send);
        return sendToClient;
    }
}