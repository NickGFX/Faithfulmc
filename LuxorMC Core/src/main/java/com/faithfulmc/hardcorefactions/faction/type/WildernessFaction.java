package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.command.CommandSender;
import org.mongodb.morphia.annotations.Entity;

import java.util.Map;

@Entity(value = "faction")
public class WildernessFaction extends Faction {
    public WildernessFaction(){

    }

    public WildernessFaction(HCF hcf) {
        super("Wilderness");
    }

    public WildernessFaction(Map<String, Object> map) {
        super(map);
    }

    public String getDisplayName(CommandSender sender) {
        return ConfigurationService.WILDERNESS_COLOUR + getName();
    }
}
