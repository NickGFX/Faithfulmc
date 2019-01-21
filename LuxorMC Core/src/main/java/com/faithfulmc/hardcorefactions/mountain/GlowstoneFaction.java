package com.faithfulmc.hardcorefactions.mountain;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.cuboid.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

@Entity(value = "faction")
public class GlowstoneFaction extends MountainFaction {
    public GlowstoneFaction(){

    }

    public GlowstoneFaction(HCF plugin) {
        super("Glowstone", ChatColor.GOLD + "Glowstone Mountain");
    }

    public GlowstoneFaction(Map<String, Object> map) {
        super(map);
    }

    public Cuboid getCuboid() {
        return HCF.getInstance().getGlowstoneMountainManager().getCuboid();
    }

    public boolean allowed(Material material) {
        return material == Material.GLOWSTONE;
    }
}
