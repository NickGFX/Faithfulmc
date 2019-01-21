package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

@Entity(value = "faction")
public class NorthRoadFaction extends RoadFaction implements ConfigurationSerializable {
    public NorthRoadFaction(){

    }

    public NorthRoadFaction(HCF plugin) {
        super("NorthRoad");
        for (World world : Bukkit.getWorlds()) {
            World.Environment environment = world.getEnvironment();
            if (environment == World.Environment.THE_END) {
                continue;
            }
            if (environment == World.Environment.NETHER) {
                ROAD_WIDTH = ROAD_WIDTH_NETHER;
            } else {
                ROAD_WIDTH = ROAD_WIDTH_NORMAL;
            }
            int borderSize = ConfigurationService.BORDER_SIZES.get(environment);
            double offset = ConfigurationService.SPAWN_RADIUS_MAP.get(environment) + 1.0;
            addClaim(new Claim(this, new Location(world, -ROAD_WIDTH / 2 + 0.5, ROAD_MIN_HEIGHT, -offset), new Location(world, ROAD_WIDTH / 2, ROAD_MAX_HEIGHT, (double) (-borderSize - 1))), null);
        }
    }

    public void reset(){
        forceRemoveClaims(getClaims(), Bukkit.getConsoleSender());
        claims.clear();
        for (World world : Bukkit.getWorlds()) {
            World.Environment environment = world.getEnvironment();
            if (environment == World.Environment.THE_END) {
                continue;
            }
            if (environment == World.Environment.NETHER) {
                ROAD_WIDTH = ROAD_WIDTH_NETHER;
            } else {
                ROAD_WIDTH = ROAD_WIDTH_NORMAL;
            }
            int borderSize = ConfigurationService.BORDER_SIZES.get(environment);
            double offset = ConfigurationService.SPAWN_RADIUS_MAP.get(environment) + 1.0;
            addClaim(new Claim(this, new Location(world, -ROAD_WIDTH / 2 + 0.5, ROAD_MIN_HEIGHT, -offset), new Location(world, ROAD_WIDTH / 2, ROAD_MAX_HEIGHT, (double) (-borderSize - 1))), null);
        }
    }

    public NorthRoadFaction(Map<String, Object> map) {
        super(map);
    }
}
