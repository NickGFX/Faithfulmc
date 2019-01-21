package com.faithfulmc.framework.hideplayers;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.util.cuboid.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerHiddenManager {
    private final File file;
    private final YamlConfiguration yamlConfiguration = new YamlConfiguration();
    private final List<Cuboid> hiddenAreas;

    public PlayerHiddenManager(){
        file = new File(BasePlugin.getPlugin().getDataFolder(), "hiddenCuboids.yml");
        if(file.exists()){
            try {
                yamlConfiguration.load(file);
            } catch (IOException|InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        hiddenAreas = (List<Cuboid>) yamlConfiguration.get("hiddenAreas", new ArrayList<>());
    }

    public void save(){
        yamlConfiguration.set("hiddenAreas", hiddenAreas.toArray(new Cuboid[hiddenAreas.size()]));
        try {
            yamlConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Cuboid> getHiddenAreas() {
        return hiddenAreas;
    }

    public boolean withinCuboid(World world, int posX, int posZ, Cuboid cuboid){
        if(cuboid.getWorld().getUID() == world.getUID()){
            Location maximumPoint = cuboid.getMaximumPoint();
            Location minimumPoint = cuboid.getMinimumPoint();
            if(maximumPoint.getBlockX() >= posX &&
                    maximumPoint.getBlockZ() >= posZ &&
                    minimumPoint.getBlockX() <= posX &&
                    minimumPoint.getBlockZ() <= posZ){
                return true;
            }
        }
        return false;
    }

    public void addCuboid(Cuboid cuboid){
        hiddenAreas.add(cuboid);
        for(Player player: Bukkit.getOnlinePlayers()){
            Location location = player.getLocation();
            World world = location.getWorld();
            int posX = location.getBlockX();
            int posZ = location.getBlockZ();
            if(withinCuboid(world, posX, posZ, cuboid)){
                ((CraftPlayer) player).getHandle().setHidden(true);
            }
        }
    }

    public void removeCuboid(Cuboid cuboid){
        hiddenAreas.remove(cuboid);
        for(Player player: Bukkit.getOnlinePlayers()){
            if(!shouldBeHidden(player.getLocation())){
                ((CraftPlayer) player).getHandle().setHidden(false);
            }
        }
    }

    public void removeCuboids(){
        for(Player player: Bukkit.getOnlinePlayers()){
            ((CraftPlayer) player).getHandle().setHidden(false);
        }
        hiddenAreas.clear();
    }

    public boolean shouldBeHidden(Location location){
        World world = location.getWorld();
        int posX = location.getBlockX();
        int posZ = location.getBlockZ();
        for(Cuboid cuboid: hiddenAreas){
            if(withinCuboid(world, posX, posZ, cuboid)){
                return true;
            }
        }
        return false;
    }
}
