package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.HCFClass;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand implements CommandExecutor, TabCompleter {
    private final String MAIN_COLOR;
    private final String SECONDARY_COLOR;
    private final String EXTRA_COLOR;
    private final String VALUE_COLOR;
    private final String STAR;

    public HelpCommand() {
        this.MAIN_COLOR = ConfigurationService.GOLD + ChatColor.BOLD.toString();
        this.SECONDARY_COLOR = ConfigurationService.YELLOW.toString();
        this.EXTRA_COLOR = ChatColor.DARK_GRAY.toString();
        this.VALUE_COLOR = ConfigurationService.GRAY.toString();
        STAR = ConfigurationService.ARROW_COLOR + " * ";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(EXTRA_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ConfigurationService.SCOREBOARD_TITLE);
        sender.sendMessage(EXTRA_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        if (ConfigurationService.KIT_MAP) {
            sender.sendMessage(MAIN_COLOR + "Information");
            sender.sendMessage(STAR + SECONDARY_COLOR + "World Border " + VALUE_COLOR + ConfigurationService.BORDER_SIZES.get(World.Environment.NORMAL) + " Blocks");
            sender.sendMessage(STAR + SECONDARY_COLOR + "Warzone Radius " + VALUE_COLOR + ConfigurationService.WARZONE_RADIUS + " Blocks");
            sender.sendMessage(STAR + SECONDARY_COLOR + "Kits " + VALUE_COLOR + "Diamond, Bard, Archer, Rogue, Builder");
            sender.sendMessage(STAR + SECONDARY_COLOR + "End Exit " + VALUE_COLOR + "(" + HCF.getInstance().getEndExit().getBlockX() + "," + HCF.getInstance().getEndExit().getBlockZ() + ") South Road");
        } else {
            sender.sendMessage(MAIN_COLOR + "Map Information");
            sender.sendMessage(STAR + SECONDARY_COLOR + "World Border " + VALUE_COLOR + ConfigurationService.BORDER_SIZES.get(World.Environment.NORMAL) + " Blocks");
            sender.sendMessage(STAR + SECONDARY_COLOR + "Warzone Radius " + VALUE_COLOR + ConfigurationService.WARZONE_RADIUS + " Blocks");
            sender.sendMessage(STAR + SECONDARY_COLOR + "End Portals " + VALUE_COLOR + "(" + ConfigurationService.END_PORTAL_LOCATION + "," + ConfigurationService.END_PORTAL_LOCATION + ") Each Quadrant");
            sender.sendMessage(STAR + SECONDARY_COLOR + "End Exit " + VALUE_COLOR + "(" + HCF.getInstance().getEndExit().getBlockX() + "," + HCF.getInstance().getEndExit().getBlockZ() + ") South Road");
            sender.sendMessage(STAR + SECONDARY_COLOR + "Classes " + VALUE_COLOR + HCF.getInstance().getHcfClassManager().getPvpClasses().stream().map(HCFClass::getName).collect(Collectors.joining(", ")));
        }
        sender.sendMessage("");
        sender.sendMessage(MAIN_COLOR + "Useful Commands");
        sender.sendMessage(STAR + SECONDARY_COLOR + "/helpop " + VALUE_COLOR + "Request staff assistance");
        sender.sendMessage(STAR + SECONDARY_COLOR + "/coords " + VALUE_COLOR + "View KOTH, Conquest and Mountain locations");
        sender.sendMessage(STAR + SECONDARY_COLOR + "/mapkit " + VALUE_COLOR + "Get the kit of the map");
        sender.sendMessage("");
        sender.sendMessage(MAIN_COLOR + "Links");
        sender.sendMessage(STAR + SECONDARY_COLOR + "Website " + VALUE_COLOR + ConfigurationService.SITE);
        sender.sendMessage(STAR + SECONDARY_COLOR + "Teamspeak " + VALUE_COLOR + ConfigurationService.TEAMSPEAK);
        sender.sendMessage(STAR + SECONDARY_COLOR + "Store " + VALUE_COLOR + ConfigurationService.STORE);
        sender.sendMessage(EXTRA_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return java.util.Collections.emptyList();
    }
}
