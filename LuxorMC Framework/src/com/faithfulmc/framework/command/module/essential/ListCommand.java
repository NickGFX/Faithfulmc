package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.listener.PlayerLimitListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ListCommand extends BaseCommand {
    public ListCommand() {
        super("list", "Lists players online");
        this.setAliases(new String[]{"who"});
        this.setUsage("/(command)");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m--&8&m---------------&6&m-----&8&m---------------&6&m--"));
        sender.sendMessage(BaseConstants.YELLOW + "There are " + PlayerLimitListener.getOnlineCount() + "/" + Bukkit.getMaxPlayers() + " players online.");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&m--&8&m---------------&6&m-----&8&m---------------&6&m--"));
        return true;
    }
}
