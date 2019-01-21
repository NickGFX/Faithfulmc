package com.faithfulmc.framework.command.module.essential.hidden.arguments;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.util.command.CommandArgument;
import com.faithfulmc.util.cuboid.Cuboid;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HiddenCreate extends CommandArgument{
    public HiddenCreate() {
        super("create", "Creates a new hidden region");
        isPlayerOnly = true;
    }

    public String getUsage(String label) {
        return "/" + label + " " + getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player && Bukkit.getPluginManager().isPluginEnabled("WorldEdit")){
            Player player = (Player) sender;
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
            Selection selection = worldEditPlugin.getSelection(player);
            if(selection == null){
                player.sendMessage(ChatColor.YELLOW + "You do not have a worldedit selection");
            }
            else{
                Cuboid cuboid = new Cuboid(selection.getMinimumPoint(), selection.getMaximumPoint());
                BasePlugin.getPlugin().getPlayerHiddenManager().addCuboid(cuboid);
                player.sendMessage(ChatColor.YELLOW + "Added cuboid");
            }
        }
        return true;
    }
}
