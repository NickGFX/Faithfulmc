package com.faithfulmc.framework.command.module.warp;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.warp.Warp;
import com.faithfulmc.util.command.CommandArgument;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WarpListArgument extends CommandArgument {
    private final BasePlugin plugin;

    public WarpListArgument(final BasePlugin plugin) {
        super("list", "List all server warps");
        this.plugin = plugin;
        this.permission = "command.warp.argument." + this.getName();
    }

    @Override
    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Collection<Warp> warps = this.plugin.getWarpManager().getWarps();
        final List<String> warpNames = new ArrayList<String>(warps.size());
        for (final Warp warp : warps) {
            if (sender.hasPermission(warp.getPermission())) {
                warpNames.add(warp.getName());
            }
        }
        sender.sendMessage(ChatColor.DARK_AQUA + "Global Warps (" + warpNames.size() + ")");
        sender.sendMessage(BaseConstants.GRAY + "[" + StringUtils.join((Collection) warpNames, ", ") + ']');
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }
}
