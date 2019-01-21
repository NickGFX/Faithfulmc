package com.faithfulmc.hardcorefactions.hcfclass.miner;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mongodb.morphia.annotations.Embedded;

import java.util.Arrays;
import java.util.List;

@Embedded
public enum MinerLevel {
            DEFAULT(0, "None"),
            BASIC(250, "Basic Miner",
                    new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0)
                    ),
            REGULAR(500, "Regular Miner",
                    new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0),
                    new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2),
                    new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 0)
                    ),
            ADVANCED(1000, "Advanced Miner",
                    new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1),
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0),
                    new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2),
                    new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 0)
                    ),
            EXPERT(1500, "Expert Miner",
                    new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1),
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0),
                    new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 3),
                    new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 1)
                    ),
            ADEPT(2000, "Adept Miner",
                    new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1),
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1),
                    new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 3),
                    new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 2)
            ),
            ELITE(2500, "Elite Miner",
                    new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1),
                    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1),
                    new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 3),
                    new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 2),
                    new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0)
            );

    private int amount;
    private String nick;
    private List<PotionEffect> give;

    MinerLevel(int amount, String nick, PotionEffect ... give) {
        this.nick = nick;
        this.give = Arrays.asList(give);
        this.amount = amount;
    }

    public String getNick() {
        return nick;
    }

    public List<PotionEffect> getGive() {
        return give;
    }

    public int getAmount() {
        return amount;
    }

    public MinerLevel next(){
        return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
    }
}
