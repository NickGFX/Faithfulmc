package com.faithfulmc.hardcorefactions.faction;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.Config;
import org.bukkit.configuration.MemorySection;

import java.util.ArrayList;
import java.util.List;

public class FlatFileFactionManager extends AbstractFactionManager {
    private Config config;

    public FlatFileFactionManager(HCF plugin) {
        super(plugin);
    }

    public void reloadFactionData() {
        this.factionNameMap.clear();
        this.config = new Config(this.plugin, "factions");
        Object object = this.config.get("factions");
        MemorySection section;
        if ((object instanceof MemorySection)) {
            section = (MemorySection) object;
            for (String factionName : section.getKeys(false)) {
                Object next = this.config.get(section.getCurrentPath() + '.' + factionName);
                if ((next instanceof Faction)) {
                    cacheFaction((Faction) next);
                }
            }
        } else if ((object instanceof List)) {
            List<?> list = (List) object;
            for (Object next2 : list) {
                if ((next2 instanceof Faction)) {
                    cacheFaction((Faction) next2);
                }
            }
        }
        addDefaults();
    }

    public void saveFactionData() {
        this.config = new Config(this.plugin, "factions");
        this.config.set("factions", new ArrayList<>(this.factionUUIDMap.values()));
        this.config.save();
    }
}
