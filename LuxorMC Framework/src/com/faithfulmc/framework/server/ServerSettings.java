package com.faithfulmc.framework.server;

import com.faithfulmc.framework.BasePlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ServerSettings {
    public static boolean SAVE_ENTRIES = true;
    public static boolean ACCEPTING_NEW = true;
    public static String NAME = BasePlugin.getPlugin().getConfig().getString("server-name");
    public static boolean HASNAME = NAME != null;

    public static void setName(String name){
        if(ACCEPTING_NEW) {
            if (SAVE_ENTRIES) {
                FileConfiguration configuration = BasePlugin.getPlugin().getConfig();
                configuration.set("server-name", name);
                BasePlugin.getPlugin().saveConfig();
            }
            NAME = name;
            HASNAME = name == null;
        }
    }
}
