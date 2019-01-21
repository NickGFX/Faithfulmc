package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

@Entity(value = "faction")
public class EndPortalFaction extends ClaimableFaction implements ConfigurationSerializable {
    public EndPortalFaction(){

    }

    public EndPortalFaction(HCF plugin) {
        super("EndPortal");
        World overworld = Bukkit.getWorld("world");
        int maxHeight = overworld.getMaxHeight();
        int min = ConfigurationService.END_PORTAL_LOCATION - 15;
        int max = ConfigurationService.END_PORTAL_LOCATION + 15;
        addClaim(new Claim(this, new Location(overworld, min, 0.0D, min), new Location(overworld, max, maxHeight, max)), null);
        addClaim(new Claim(this, new Location(overworld, -max, maxHeight, -max), new Location(overworld, -min, 0.0D, -min)), null);
        addClaim(new Claim(this, new Location(overworld, -max, 0.0D, min), new Location(overworld, -min, maxHeight, max)), null);
        addClaim(new Claim(this, new Location(overworld, min, 0.0D, -max), new Location(overworld, max, maxHeight, -min)), null);
        this.safezone = false;
    }

    public EndPortalFaction(Map<String, Object> map) {
        super(map);
    }

    public String getDisplayName(CommandSender sender) {
        return ChatColor.DARK_AQUA + getName().replace("EndPortal", "End Portal");
    }

    public boolean isDeathban() {
        return true;
    }
}
