package com.faithfulmc.hardcorefactions.util.location;

import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

public class BlockLocation implements ConfigurationSerializable {
    public static BlockLocation fromLocation(Location location) {
        return new BlockLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private final String world;
    private final int x, y, z;

    public BlockLocation(Map map) {
        world = (String) map.get("world");
        x = (Integer) map.get("x");
        y = (Integer) map.get("y");
        z = (Integer) map.get("z");
    }

    public BlockLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorld() {
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
        return "BlockLocation{" + "world='" + world + '\'' + ", x=" + x + ", y=" + y + ", z=" + z + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlockLocation that = (BlockLocation) o;

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

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("world", world);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        return map;
    }
}
