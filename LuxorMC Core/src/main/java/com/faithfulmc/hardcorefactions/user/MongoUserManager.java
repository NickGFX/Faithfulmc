package com.faithfulmc.hardcorefactions.user;

import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.logging.Level;

public class MongoUserManager extends AbstractUserManager {
    public MongoUserManager(HCF plugin) {
        super(plugin);
        new BukkitRunnable() {
            public void run() {
                tryImport();
            }
        }.runTaskAsynchronously(plugin);
    }

    public void tryImport() {
        File oldFile = new File(plugin.getDataFolder(), "faction-users.yml");
        if (oldFile.exists()) {
            plugin.getLogger().log(Level.INFO, "User file found, importing factions");
            FlatfileUserManager flatfileUserManager = new FlatfileUserManager(plugin);
            inMemoryStorage.putAll(flatfileUserManager.inMemoryStorage);
            plugin.getLogger().log(Level.INFO, "Moved users to new imports");
            flatfileUserManager = null;
            saveUserData();
            plugin.getLogger().log(Level.INFO, "Saved new user data");
            File backupFile = new File(oldFile.getName() + ".backup");
            if (backupFile.exists()) {
                backupFile.delete();
            }
            oldFile.renameTo(backupFile);
            plugin.getLogger().log(Level.INFO, "Faction import complete");
        }
    }

    public void saveUserData() {
        long now = System.currentTimeMillis();
        inMemoryStorage.values().stream().filter(FactionUser::isOnline).forEach(user -> user.calcPlaytime(now));
        plugin.getMorphiastore().save(inMemoryStorage.values());
    }

    public void reloadUserData() {
        for (FactionUser factionUser : plugin.getMorphiastore().createQuery(FactionUser.class).fetch()) {
            inMemoryStorage.put(factionUser.getUserUUID(), factionUser);
            if(factionUser.getName() != null){
                uuidCache.put(factionUser.getName(), factionUser.getUserUUID());
            }
        }
    }

    @Override
    public void saveUser(FactionUser factionUser) {
        plugin.getMorphiastore().save(factionUser);
    }
}
