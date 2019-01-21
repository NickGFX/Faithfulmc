package com.faithfulmc.hardcorefactions.hcfclass.old.mage;

import org.bukkit.potion.PotionEffect;

public class MageEffect {
    public final int energyCost;
    public final PotionEffect clickable;
    public final PotionEffect heldable;

    public MageEffect(int energyCost, PotionEffect clickable, PotionEffect heldable) {
        this.energyCost = energyCost;
        this.clickable = clickable;
        this.heldable = heldable;
    }
}
