package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

@Entity(value = "faction")
public class EndSafezoneFaction extends ClaimableFaction implements ConfigurationSerializable {
    public EndSafezoneFaction(){

    }

    public EndSafezoneFaction(HCF plugin) {
        super("EndSafezone");
        this.safezone = true;
        for (World world : Bukkit.getWorlds()) {
            World.Environment environment = world.getEnvironment();
            if (environment == World.Environment.THE_END) {
                double radius = 5.0;
                addClaim(new Claim(this, new Location(world, radius, 0.0D, radius), new Location(world, -radius, world.getMaxHeight(), -radius)), null);
            }
        }
    }

    public void reset(){
        forceRemoveClaims(getClaims(), Bukkit.getConsoleSender());
        claims.clear();
        for (World world : Bukkit.getWorlds()) {
            World.Environment environment = world.getEnvironment();
            if (environment == World.Environment.THE_END) {
                double radius = 5.0;
                addClaim(new Claim(this, new Location(world, radius, 0.0D, radius), new Location(world, -radius, world.getMaxHeight(), -radius)), null);
            }
        }
    }

    public EndSafezoneFaction(Map<String, Object> map) {
        super(map);
    }

    public boolean isDeathban() {
        return false;
    }
}
