package com.faithfulmc.hardcorefactions.hcfclass.bard;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.google.common.base.Preconditions;

public class BardData {
    public static final double MIN_ENERGY = 0.0;
    public static final double MAX_ENERGY = 100.0;
    public static final long MAX_ENERGY_MILLIS = 100000;
    private static final double ENERGY_PER_MILLISECOND = ConfigurationService.ORIGINS ? 1 : 1.25;
    public long buffCooldown;
    private long energyStart;

    public long getRemainingBuffDelay() {
        return this.buffCooldown - System.currentTimeMillis();
    }

    public long getRemainingBuffDelay(long now) {
        return this.buffCooldown - now;
    }

    public void startEnergyTracking() {
        this.setEnergy(MIN_ENERGY);
    }

    public long getEnergyMillis() {
        if (this.energyStart == 0) {
            return 0;
        }
        return Math.min(MAX_ENERGY_MILLIS, (long) (ENERGY_PER_MILLISECOND * (double) (System.currentTimeMillis() - this.energyStart)));
    }

    public long getEnergyMillis(long now) {
        if (this.energyStart == 0) {
            return 0;
        }
        return Math.min(MAX_ENERGY_MILLIS, (long) (ENERGY_PER_MILLISECOND * (double) (now - this.energyStart)));
    }

    public double getEnergy() {
        double value = (double) this.getEnergyMillis() / 1000.0;
        return (double) Math.round(value * 10.0) / 10.0;
    }

    public void setEnergy(double energy) {
        Preconditions.checkArgument((energy >= 0.0), "Energy cannot be less than 0.0");
        Preconditions.checkArgument((energy <= 100.0), "Energy cannot be more than 100.0");
        this.energyStart = (long) ((double) System.currentTimeMillis() - 1000.0 * energy);
    }
}