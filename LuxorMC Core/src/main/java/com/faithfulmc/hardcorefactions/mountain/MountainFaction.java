package com.faithfulmc.hardcorefactions.mountain;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.cuboid.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

@Entity(value = "faction")
public abstract class MountainFaction extends ClaimableFaction {
    private String colorname;

    public MountainFaction(){

    }

    public MountainFaction(String name, String colorname) {
        super(name);
        this.colorname = colorname;
    }

    public MountainFaction(Map<String, Object> map) {
        super(map);
        colorname = (String) map.get("colorname");
    }

    public abstract Cuboid getCuboid();

    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("colorname", colorname);
        return map;
    }

    public void reload() {
        removeClaims(getClaims(), Bukkit.getConsoleSender());
        if (ConfigurationService.KIT_MAP) {
            return;
        }
        Cuboid cuboid = getCuboid();
        if (cuboid != null) {
            addClaim(new Claim(this, cuboid), Bukkit.getConsoleSender());
        }
    }

    public abstract boolean allowed(Material material);

    public boolean isSafezone() {
        return false;
    }

    public boolean isDeathban() {
        return true;
    }

    public String getDisplayName(Faction other) {
        return colorname;
    }

    public String getDisplayName(CommandSender sender) {
        return colorname;
    }

    public void printDetails(CommandSender sender) {
        Bukkit.dispatchCommand(sender, name);
    }
}
