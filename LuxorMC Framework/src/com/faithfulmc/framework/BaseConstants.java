package com.faithfulmc.framework;

import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;

public final class BaseConstants {
    public static final String PLAYER_WITH_NAME_OR_UUID_NOT_FOUND;
    public static String DOUBLEARROW = "»";
    public static ChatColor YELLOW = ChatColor.YELLOW;
    public static ChatColor GOLD = ChatColor.GOLD;
    public static ChatColor GRAY = ChatColor.GRAY;
    public static String NAME = "Faithful",SITE = "www.faithfulmc.com", TEAMSPEAK = "ts.faithfulmc.com";

    static {
        PLAYER_WITH_NAME_OR_UUID_NOT_FOUND = GOLD + "Player with name or UUID '" + ChatColor.WHITE + "%1$s" + GOLD + "' not found.";
    }

    public static net.md_5.bungee.api.ChatColor fromBukkit(ChatColor chatColor){
        return net.md_5.bungee.api.ChatColor.values()[chatColor.ordinal()];
    }

    public static void load(MemorySection memorySection){
        YELLOW = ChatColor.getByChar(memorySection.getString("colors.yellow", String.valueOf(ChatColor.YELLOW.getChar())));
        GOLD = ChatColor.getByChar(memorySection.getString("colors.gold", String.valueOf(ChatColor.GOLD.getChar())));
        GRAY = ChatColor.getByChar(memorySection.getString("colors.gray", String.valueOf(ChatColor.GRAY.getChar())));
        NAME = memorySection.getString("info.name", "Faithful");
        SITE = memorySection.getString("info.site", "www.faithfulmc.com");
        TEAMSPEAK = memorySection.getString("info.teamspeak", "ts.faithfulmc.com");
    }
}
