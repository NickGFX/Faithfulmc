package com.faithfulmc.hardcorefactions.faction.type;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.Map;

@Entity(value = "faction")
public class RoadFaction extends ClaimableFaction implements ConfigurationSerializable {
    public static final int ROAD_WIDTH_NETHER = 14;
    public static final int ROAD_WIDTH_NORMAL = 36;
    public static final int ROAD_MIN_HEIGHT = 0;
    public static final int ROAD_MAX_HEIGHT = 256;
    public static int ROAD_WIDTH = 16;

    @Transient
    private String formattedName = null;

    public RoadFaction(){

    }

    public RoadFaction(String name) {
        super(name);
    }

    public RoadFaction(Map<String, Object> map) {
        super(map);
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        if(formattedName == null){
            formattedName = getName().replace("st", "st ").replace("th", "th ");
        }
        return ConfigurationService.ROAD_COLOUR + formattedName;
    }

    @Override
    public void printDetails(CommandSender sender) {
        sender.sendMessage( ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(' ' + getDisplayName(sender));
        sender.sendMessage( ConfigurationService.YELLOW + "  Location: " + ConfigurationService.GRAY + "None");
        sender.sendMessage( ConfigurationService.LINE_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

}