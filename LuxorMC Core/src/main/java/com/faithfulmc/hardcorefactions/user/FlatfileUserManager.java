package com.faithfulmc.hardcorefactions.user;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.Config;
import org.bukkit.configuration.MemorySection;

import java.util.*;

public class FlatfileUserManager extends AbstractUserManager {
    private Config userConfig;

    public FlatfileUserManager(HCF plugin) {
        super(plugin);
    }

    public void reloadUserData() {
        userConfig = new Config(this.plugin, "faction-users");
        Object object = userConfig.get("users");
        MemorySection section;
        if ((object instanceof MemorySection)) {
            section = (MemorySection) object;
            Collection<String> keys = section.getKeys(false);
            for (String id : keys) {
                UUID uuid = UUID.fromString(id);
                FactionUser factionUser = (FactionUser) this.userConfig.get(section.getCurrentPath() + '.' + id);
                this.inMemoryStorage.put(uuid, factionUser);
                if(factionUser.getName() != null){
                    uuidCache.put(factionUser.getName(), uuid);
                }
            }
        }
    }

    public void saveUserData() {
        Set<Map.Entry<UUID, FactionUser>> entrySet = this.inMemoryStorage.entrySet();
        Map<String, FactionUser> saveMap = new LinkedHashMap<>(entrySet.size());
        for (Map.Entry<UUID, FactionUser> entry : entrySet) {
            saveMap.put((entry.getKey()).toString(), entry.getValue());
        }
        userConfig.set("users", saveMap);
        userConfig.save();
    }
}
