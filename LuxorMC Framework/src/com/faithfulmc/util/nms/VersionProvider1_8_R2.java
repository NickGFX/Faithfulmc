package com.faithfulmc.util.nms;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EnumProtocol;
import net.minecraft.server.v1_8_R3.NetworkManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class VersionProvider1_8_R2 implements NMSProvider{
    public VersionProvider1_8_R2() {
    }

    public Object getNMSHandle(ItemStack stack){
        return CraftItemStack.asNMSCopy(stack);
    }

    public String getName(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack).getName();
    }

    public int getVersion(Player player) {
        NetworkManager networkManager = ((CraftPlayer)player).getHandle().playerConnection.networkManager;
        EnumProtocol ver = networkManager.channel.attr(NetworkManager.c).get();
        return ver != null?ver.a():48;
    }

    public Entity getEntityFromID(World world, int id) {
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftWorld)world).getHandle().a(id);
        if(entity != null){
            return entity.getBukkitEntity();
        }
        return null;
    }

    public long getIdleTime(Player player){
        return ((CraftPlayer)player).getHandle().D();
    }

    public Chunk setBlockFast(Block b, MaterialData materialData) {
        Chunk c = b.getChunk();
        net.minecraft.server.v1_8_R3.Chunk chunk = ((CraftChunk) c).getHandle();
        chunk.a(new BlockPosition(b.getX() & 15, b.getY(), b.getZ() & 15), net.minecraft.server.v1_8_R3.Block.getById(materialData.getItemTypeId()).fromLegacyData(materialData.getData()));
        return c;
    }

}