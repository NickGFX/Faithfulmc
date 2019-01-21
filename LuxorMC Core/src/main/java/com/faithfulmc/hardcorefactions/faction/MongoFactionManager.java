package com.faithfulmc.hardcorefactions.faction;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.FactionRemoveEvent;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.logging.Level;

public class MongoFactionManager extends AbstractFactionManager {
    public MongoFactionManager(HCF plugin) {
        super(plugin);
        new BukkitRunnable() {
            public void run() {
                tryImport();
            }
        }.runTaskAsynchronously(plugin);
    }

    public void tryImport() {
        File oldFile = new File(plugin.getDataFolder(), "factions.yml");
        if (oldFile.exists()) {
            plugin.getLogger().log(Level.INFO, "Faction file found, importing factions");
            FlatFileFactionManager flatFileFactionManager = new FlatFileFactionManager(plugin);
            plugin.getLogger().log(Level.INFO, "Loaded factions from disk");
            factionUUIDMap.putAll(flatFileFactionManager.factionUUIDMap);
            factionNameMap.putAll(flatFileFactionManager.factionNameMap);
            claimPositionTable.putAll(flatFileFactionManager.claimPositionTable);
            positionCache.invalidateAll();
            cacheFaction(warzone);
            cacheFaction(wilderness);
            plugin.getLogger().log(Level.INFO, "Moved factions to new imports");
            flatFileFactionManager = null;
            saveFactionData();
            plugin.getLogger().log(Level.INFO, "Saved new faction data");
            File backupFile = new File(oldFile.getName() + ".backup");
            if (backupFile.exists()) {
                backupFile.delete();
            }
            oldFile.renameTo(backupFile);
            plugin.getLogger().log(Level.INFO, "Faction import complete");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFactionRemove(FactionRemoveEvent event) {
        new BukkitRunnable() {
            public void run() {
                removeFaction(event.getFaction());
            }
        }.runTaskAsynchronously(plugin);
    }

    public void removeFaction(Faction faction) {
        plugin.getMorphiastore().delete(faction);
    }

    public void updateFaction(Faction faction) {
        plugin.getMorphiastore().save(faction);
    }

    public void updateAll() {
        plugin.getMorphiastore().save(factionUUIDMap.values());
    }

    public void reloadFactionData() {
        this.factionNameMap.clear();
        for (Faction faction : plugin.getMorphiastore().createQuery(Faction.class).fetch()) {
            //if(!(faction instanceof PlayerFaction) || ((PlayerFaction) faction).getMembers().size() > 0) {
                cacheFaction(faction);
            //}
        }
        addDefaults();
    }

    public void saveFactionData() {
        updateAll();
    }
}
