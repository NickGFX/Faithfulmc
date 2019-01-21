package com.faithfulmc.hardcorefactions.timer;


import com.faithfulmc.util.Config;

public abstract class Timer {
    protected final String name;
    public final long defaultCooldown;


    public Timer(String name, long defaultCooldown) {
        this.name = name;
        this.defaultCooldown = defaultCooldown;

    }


    public abstract String getScoreboardPrefix();


    public String getName() {
        return this.name;

    }


    public final String getDisplayName() {
        return getScoreboardPrefix() + this.name;
    }


    public void load(Config config) {
    }


    public void save(Config config) {
    }

}