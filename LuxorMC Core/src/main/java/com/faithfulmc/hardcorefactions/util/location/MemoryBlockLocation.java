package com.faithfulmc.hardcorefactions.util.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class MemoryBlockLocation{
    public static MemoryBlockLocation fromLocation(Location location) {
        return new MemoryBlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private final World world;
    private final int x, y, z;

    public MemoryBlockLocation(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "BlockLocation{" + "world='" + world.getName() + '\'' + ", x=" + x + ", y=" + y + ", z=" + z + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MemoryBlockLocation that = (MemoryBlockLocation) o;

        if (x != that.x) {
            return false;
        }
        if (y != that.y) {
            return false;
        }
        if (z != that.z) {
            return false;
        }
        return world.equals(that.world);

    }

    public Block getBlock(){
        return world.getBlockAt(x, y, z);
    }

    public Location toLocation(){
        return new Location(world, x, y, z);
    }

    @Override
    public int hashCode() {
        int result = world.getName().hashCode();
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}
