package com.faithfulmc.hardcorefactions.util;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum ArmorType {
    LEATHER("Leather", Material.LEATHER, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS),
    GOLD("Gold", Material.GOLD_INGOT, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS),
    IRON("Iron", Material.IRON_INGOT, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS),
    DIAMOND("Diamond", Material.DIAMOND, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);

    private final String name;
    private final Material craft;
    private final List<Material> types;

    ArmorType(String name, Material craft, Material... types) {
        this.name = name;
        this.craft = craft;
        this.types = Arrays.asList(types);
    }

    public String getName() {
        return name;
    }

    public Material getCraft() {
        return craft;
    }

    public List<Material> getTypes() {
        return types;
    }
}
