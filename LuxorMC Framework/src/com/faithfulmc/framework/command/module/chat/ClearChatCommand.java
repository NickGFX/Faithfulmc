package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearChatCommand extends BaseCommand {
    private static final int CHAT_HEIGHT = 101;
    private static final String[] CLEAR_MESSAGE;

    static {
        CLEAR_MESSAGE = new String[101];
    }

    public ClearChatCommand() {
        super("clearchat", "Clears the server chat for players.");
        this.setAliases(new String[]{"cc"});
        this.setUsage("/(command) <reason>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + this.getUsage());
            return true;
        }
        final String reason = StringUtils.join((Object[]) args, ' ');
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ClearChatCommand.CLEAR_MESSAGE);
            if (player.hasPermission("base.command.clearchat")) {
                player.sendMessage(ChatColor.DARK_AQUA + sender.getName() + BaseConstants.YELLOW + " has cleared chat for: " + reason);
            }
        }
        Bukkit.getConsoleSender().sendMessage(BaseConstants.YELLOW + sender.getName() + " cleared in-game chat.");
        return true;
    }
}
