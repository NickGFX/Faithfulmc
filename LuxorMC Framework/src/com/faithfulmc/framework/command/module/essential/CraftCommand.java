package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CraftCommand extends BaseCommand {
    public CraftCommand() {
        super("craft", "Opens a workbench inventory.");
        this.setAliases(new String[]{"workbench", "wbench"});
        this.setUsage("/(command)");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }
        final Player player = (Player) sender;
        player.openWorkbench(player.getLocation(), true);
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }
}
