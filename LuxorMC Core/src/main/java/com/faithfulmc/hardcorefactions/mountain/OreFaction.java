package com.faithfulmc.hardcorefactions.mountain;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.cuboid.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

@Entity(value = "faction")
public class OreFaction extends MountainFaction {
    public OreFaction(){

    }

    public OreFaction(HCF plugin) {
        super("OreFaction", ChatColor.BLUE + "Ore Mountain");
    }

    public OreFaction(Map<String, Object> map) {
        super(map);
    }

    public Cuboid getCuboid() {
        return HCF.getInstance().getOreMountainManager().getCuboid();
    }

    public boolean allowed(Material material) {
        return HCF.getInstance().getOreMountainManager().ALLOWED.contains(material.getId());
    }
}
