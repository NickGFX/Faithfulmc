package com.faithfulmc.framework.command.module.warp;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.warp.Warp;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class WarpSetArgument extends CommandArgument {
    private final BasePlugin plugin;

    public WarpSetArgument(final BasePlugin plugin) {
        super("set", "Sets a new server warps");
        this.plugin = plugin;
        this.aliases = new String[]{"create", "make"};
        this.permission = "command.warp.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + ' ' + this.getName() + " <warpName>");
            return true;
        }
        if (this.plugin.getWarpManager().getWarp(args[1]) != null) {
            sender.sendMessage(ChatColor.RED + "There is already a warp named " + args[1] + '.');
            return true;
        }
        final Player player = (Player) sender;
        final Location location = player.getLocation();
        final Warp warp = new Warp(args[1], location);
        this.plugin.getWarpManager().createWarp(warp);
        sender.sendMessage(BaseConstants.GRAY + "Created a global warp named " + ChatColor.BLUE + warp.getName() + BaseConstants.GRAY + " with permission " + ChatColor.BLUE + warp.getPermission() + BaseConstants.GRAY + '.');
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }
}
