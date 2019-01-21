package com.faithfulmc.util.nms;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class VersionProvider1_7_R1 implements NMSProvider{
    public VersionProvider1_7_R1() {
    }

    public Object getNMSHandle(ItemStack stack) {
        return CraftItemStack.asNMSCopy(stack);
    }

    public String getName(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack).getName();
    }

    public int getVersion(Player player) {
        return ((CraftPlayer)player).getHandle().playerConnection.networkManager.getVersion();
    }

    public Entity getEntityFromID(World world, int id) {
        net.minecraft.server.v1_7_R4.Entity entity = ((CraftWorld)world).getHandle().getEntity(id);
        if(entity != null){
            return entity.getBukkitEntity();
        }
        return null;
    }

    public long getIdleTime(Player player){
        return ((CraftPlayer)player).getHandle().x();
    }

    public Chunk setBlockFast(Block b, MaterialData materialData) {
        Chunk c = b.getChunk();
        net.minecraft.server.v1_7_R4.Chunk chunk = ((CraftChunk) c).getHandle();
        chunk.a(b.getX() & 15, b.getY(), b.getZ() & 15, net.minecraft.server.v1_7_R4.Block.getById(materialData.getItemTypeId()), materialData.getData());
        return c;
    }

}
