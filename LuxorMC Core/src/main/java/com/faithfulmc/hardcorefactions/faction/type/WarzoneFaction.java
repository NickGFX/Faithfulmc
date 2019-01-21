package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.command.CommandSender;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

@Entity(value = "faction")
public class WarzoneFaction extends Faction {
    public WarzoneFaction(){

    }

    public WarzoneFaction(HCF hcf) {
        super("Warzone");
    }

    public WarzoneFaction(Map<String, Object> map) {
        super(map);
    }

    public String getDisplayName(CommandSender sender) {
        return ConfigurationService.WARZONE_COLOUR + getName();
    }
}
