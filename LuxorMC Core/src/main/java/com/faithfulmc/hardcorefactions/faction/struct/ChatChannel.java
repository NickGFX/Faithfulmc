package com.faithfulmc.hardcorefactions.faction.struct;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import org.bukkit.entity.Player;
import org.mongodb.morphia.annotations.Embedded;

import java.util.Locale;

@Embedded
public enum ChatChannel {
    FACTION("Faction"), ALLIANCE("Alliance"), PUBLIC("Public");

    public static ChatChannel parse(String id) {
        return parse(id, PUBLIC);
    }

    public static ChatChannel parse(String id, ChatChannel def) {
        String s;
        String lowerCase = s = id = id.toLowerCase(Locale.ENGLISH);
        switch (s) {
            case "f":
            case "faction":
            case "fc":
            case "fac":
            case "fact":
                return FACTION;
            case "a":
            case "alliance":
            case "ally":
            case "ac":
                return ALLIANCE;
            case "p":
            case "pc":
            case "g":
            case "gc":
            case "global":
            case "pub":
            case "publi":
            case "public":
                return PUBLIC;
        }
        return def == null ? null : def.getRotation();
    }

    private final String name;

    private ChatChannel(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        String prefix = null;
        switch (this) {
            case FACTION:
                prefix = ConfigurationService.TEAMMATE_COLOUR.toString();
                break;
            case ALLIANCE:
                prefix = ConfigurationService.ALLY_COLOUR.toString();
                break;
            default:
                prefix = ConfigurationService.ENEMY_COLOUR.toString();
        }
        return prefix + this.name;
    }

    public String getShortName() {
        switch (this) {
            case FACTION:
                return "FC";
            case ALLIANCE:
                return "AC";
        }
        return "PC";
    }

    public ChatChannel getRotation() {
        switch (this) {
            case FACTION:
                return PUBLIC;
            case PUBLIC:
                return ALLIANCE;
            case ALLIANCE:
                return FACTION;
        }
        return PUBLIC;
    }

    public String getRawFormat(Player player) {
        switch (this) {
            case FACTION:
                return ConfigurationService.TEAMMATE_COLOUR + "(" + getDisplayName() + ConfigurationService.TEAMMATE_COLOUR + ") " + player.getName() + ConfigurationService.GRAY + ": " + ConfigurationService.YELLOW + "%2$s";
            case ALLIANCE:
                return ConfigurationService.ALLY_COLOUR + "(" + getDisplayName() + ConfigurationService.ALLY_COLOUR + ") " + player.getName() + ConfigurationService.GRAY + ": " + ConfigurationService.YELLOW + "%2$s";
        }
        throw new IllegalArgumentException("Cannot get the raw format for public chat channel");
    }
}
