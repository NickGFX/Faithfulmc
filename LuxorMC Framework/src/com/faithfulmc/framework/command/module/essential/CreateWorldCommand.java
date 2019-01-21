package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CreateWorldCommand extends BaseCommand {
    HashMap flags;

    public CreateWorldCommand() {
        super("createworld", "Creates a world");
        this.flags = new HashMap();
        this.setUsage("/(command) [worldname]");
        this.setAliases(new String[]{"cw", "createw", "worldgen", "cworld"});
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 1) {
            if (Bukkit.getWorld(args[0]) != null) {
                sender.sendMessage(ChatColor.RED + "That world already exists.");
                return true;
            }
            Bukkit.createWorld(new WorldCreator(args[0]).environment(World.Environment.NORMAL).type(WorldType.FLAT));
            sender.sendMessage(BaseConstants.GOLD + "The world with the name '" + ChatColor.WHITE + args[0] + BaseConstants.GOLD + "' is being created.");
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + this.getUsage());
            return false;
        }
        return true;
    }
}
