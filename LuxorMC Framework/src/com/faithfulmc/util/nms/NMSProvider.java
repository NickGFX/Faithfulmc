package com.faithfulmc.util.nms;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public interface NMSProvider {
    Object getNMSHandle(ItemStack stack);
    String getName(ItemStack itemStack);
    int getVersion(Player player);
    Entity getEntityFromID(World world, int id);
    long getIdleTime(Player player);
    Chunk setBlockFast(Block b, MaterialData materialData);
}
