package com.faithfulmc.hardcorefactions.hcfclass;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.archer.ArcherClass;
import com.faithfulmc.hardcorefactions.hcfclass.bard.BardClass;
import com.faithfulmc.hardcorefactions.hcfclass.event.PvpClassEquipEvent;
import com.faithfulmc.hardcorefactions.hcfclass.event.PvpClassUnequipEvent;
import com.faithfulmc.hardcorefactions.hcfclass.miner.MinerClass;
import com.faithfulmc.hardcorefactions.hcfclass.rogue.RogueClass;
import com.faithfulmc.hardcorefactions.util.Cooldowns;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HCFClassManager {
    private ConcurrentMap<UUID, HCFClass> equippedClass;
    private Map<String, HCFClass> pvpClasses;

    public HCFClassManager(final HCF plugin) {
        this.equippedClass = new ConcurrentHashMap<>();
        this.pvpClasses = new HashMap<>();
        if(ConfigurationService.HCF_CLASSES.getOrDefault("Archer", true)) {
            this.pvpClasses.put("Archer", new ArcherClass(plugin));
        }
        if(ConfigurationService.HCF_CLASSES.getOrDefault("Bard", true)) {
            this.pvpClasses.put("Bard", new BardClass(plugin));
        }
        if(ConfigurationService.HCF_CLASSES.getOrDefault("Rogue", false)) {
            this.pvpClasses.put("Rogue", new RogueClass(plugin));
        }
        if(ConfigurationService.HCF_CLASSES.getOrDefault("Miner", true)) {
            this.pvpClasses.put(ConfigurationService.KIT_MAP ? "Builder" : "Miner", new MinerClass(plugin));
        }
        for (HCFClass pvpClass : this.pvpClasses.values()) {
            if (ConfigurationService.KIT_MAP || ConfigurationService.ORIGINS) {
                pvpClass.warmupDelay = 250;
            }
            plugin.getServer().getPluginManager().registerEvents(pvpClass, plugin);
            if(pvpClass.hasBuffs()){
                Cooldowns.createCooldown(pvpClass.getCooldown(), pvpClass.buffDelay);
            }
        }
    }

    public void onDisable() {
        for (Map.Entry<UUID, HCFClass> entry : new HashMap<>(this.equippedClass).entrySet()) {
            setEquippedClass(Bukkit.getPlayer(entry.getKey()), null);
        }
        this.pvpClasses.clear();
        this.equippedClass.clear();
    }

    public Collection<HCFClass> getPvpClasses() {
        return this.pvpClasses.values();
    }

    public HCFClass getPvpClass(final String name) {
        return this.pvpClasses.get(name);
    }

    public HCFClass getEquippedClass(final Player player) {
        return this.equippedClass.get(player.getUniqueId());
    }

    public boolean hasClassEquipped(final Player player, final HCFClass pvpClass) {
        final HCFClass equipped = this.getEquippedClass(player);
        return equipped != null && equipped.equals(pvpClass);
    }

    public void setEquippedClass(final Player player, @Nullable final HCFClass pvpClass) {
        HCFClass equipped = this.getEquippedClass(player);
        if (equipped != null) {
            if (pvpClass == null) {
                this.equippedClass.remove(player.getUniqueId());
                equipped.onUnequip(player);
                Bukkit.getPluginManager().callEvent(new PvpClassUnequipEvent(player, equipped));
                return;
            }
        } else if (pvpClass == null) {
            return;
        }
        if (pvpClass.onEquip(player)) {
            this.equippedClass.put(player.getUniqueId(), pvpClass);
            Bukkit.getPluginManager().callEvent(new PvpClassEquipEvent(player, pvpClass));
        }
    }
}