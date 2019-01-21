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
public class WestRoadFaction extends RoadFaction implements ConfigurationSerializable {
    public WestRoadFaction(){

    }

    public WestRoadFaction(HCF plugin) {
        super("WestRoad");
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
            this.addClaim(new Claim(this, new Location(world, -offset, ROAD_MIN_HEIGHT, ROAD_WIDTH / 2), new Location(world, (double) (-borderSize - 1), ROAD_MAX_HEIGHT, -ROAD_WIDTH / 2 + 0.5)), null);
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
            this.addClaim(new Claim(this, new Location(world, -offset, ROAD_MIN_HEIGHT, ROAD_WIDTH / 2), new Location(world, (double) (-borderSize - 1), ROAD_MAX_HEIGHT, -ROAD_WIDTH / 2 + 0.5)), null);
        }
    }

    public WestRoadFaction(Map<String, Object> map) {
        super(map);
    }
}
