package com.faithfulmc.hardcorefactions.hcfclass.bard;

import org.bukkit.potion.PotionEffect;

public class BardEffect {
    public final int energyCost;
    public final PotionEffect clickable;
    public final PotionEffect heldable;

    public BardEffect(int energyCost, PotionEffect clickable, PotionEffect heldable) {
        this.energyCost = energyCost;
        this.clickable = clickable;
        this.heldable = heldable;
    }
}