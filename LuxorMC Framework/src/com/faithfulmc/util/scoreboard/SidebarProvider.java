package com.faithfulmc.util.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public abstract interface SidebarProvider {
    public abstract String getTitle();

    public abstract List<SidebarEntry> getLines(Player paramPlayer, long now);
}
