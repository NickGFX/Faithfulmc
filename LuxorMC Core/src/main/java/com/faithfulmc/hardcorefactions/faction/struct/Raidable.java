package com.faithfulmc.hardcorefactions.faction.struct;

public interface Raidable {
    boolean isRaidable();

    double getDeathsUntilRaidable();

    double getMaximumDeathsUntilRaidable();

    double setDeathsUntilRaidable(double paramDouble);

    long getRemainingRegenerationTime();

    void setRemainingRegenerationTime(long paramLong);

    RegenStatus getRegenStatus();
}
