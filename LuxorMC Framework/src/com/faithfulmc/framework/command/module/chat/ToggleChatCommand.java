package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleChatCommand extends BaseCommand {
    private final BasePlugin plugin;

    public ToggleChatCommand(final BasePlugin plugin) {
        super("togglechat", "Toggles global chat visibility.");
        this.setAliases(new String[]{"tgc", "toggleglobalchat"});
        this.setUsage("/(command)");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable for players.");
            return true;
        }
        final Player player = (Player) sender;
        final BaseUser baseUser = this.plugin.getUserManager().getUser(player.getUniqueId());
        final boolean newChatToggled = !baseUser.isGlobalChatVisible();
        baseUser.setGlobalChatVisible(newChatToggled);
        sender.sendMessage(BaseConstants.YELLOW + "You have toggled global chat visibility " + (newChatToggled ? (ChatColor.GREEN + "on") : (ChatColor.RED + "off")) + BaseConstants.YELLOW + '.');
        return true;
    }
}
