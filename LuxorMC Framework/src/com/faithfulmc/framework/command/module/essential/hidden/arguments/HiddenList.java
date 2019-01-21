package com.faithfulmc.framework.command.module.essential.hidden.arguments;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.util.command.CommandArgument;
import com.faithfulmc.util.cuboid.Cuboid;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HiddenList extends CommandArgument{
    public HiddenList() {
        super("list", "Lists all hidden regions");
    }

    public String getUsage(String label) {
        return "/" + label + " " + getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int i = 0;
        for(Cuboid cuboid: BasePlugin.getPlugin().getPlayerHiddenManager().getHiddenAreas()){
            sender.sendMessage(ChatColor.YELLOW + " " + (i + 1) + ". " + cuboid.getWorldName() + ChatColor.GRAY + " (" + ChatColor.WHITE + stringify(cuboid.getMinimumPoint()) + ChatColor.GRAY + " -> " + ChatColor.WHITE + stringify(cuboid.getMaximumPoint()) + ChatColor.GRAY + ")");
            i++;
        }
        return true;
    }

    public String stringify(Location location){
        return location.getBlockX() + "," + location.getBlockZ();
    }
}
