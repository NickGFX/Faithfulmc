package com.faithfulmc.framework.command.module.essential.hidden.arguments;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.util.command.CommandArgument;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HiddenRemoveAll extends CommandArgument{
    public HiddenRemoveAll() {
        super("removeall", "Removes all hidden regions");
    }

    public String getUsage(String label) {
        return "/" + label + " " + getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BasePlugin.getPlugin().getPlayerHiddenManager().removeCuboids();
        sender.sendMessage(ChatColor.YELLOW + "Removed all hidden regions");
        return true;
    }
}
