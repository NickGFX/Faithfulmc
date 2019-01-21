package com.faithfulmc.hardcorefactions.kit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public interface KitManager {
    public static final int UNLIMITED_USES = Integer.MAX_VALUE;

    List<Kit> getKits();

    Kit getKit(final String p0);

    Kit getKit(final UUID p0);

    boolean containsKit(final Kit p0);

    void createKit(final Kit p0);

    void removeKit(final Kit p0);

    Inventory getGui(final Player p0);

    void reloadKitData();

    void saveKitData();
}
