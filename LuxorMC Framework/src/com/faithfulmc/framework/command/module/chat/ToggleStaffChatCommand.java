package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.ServerParticipator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ToggleStaffChatCommand extends BaseCommand {
    private final BasePlugin plugin;

    public ToggleStaffChatCommand(final BasePlugin plugin) {
        super("togglestaffchat", "Toggles staff chat visibility.");
        this.setAliases(new String[]{"tsc", "togglesc"});
        this.setUsage("/(command)");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
        if (participator == null) {
            sender.sendMessage(ChatColor.RED + "You are not allowed to do this.");
            return true;
        }
        final boolean newChatToggled = !participator.isStaffChatVisible();
        participator.setStaffChatVisible(newChatToggled);
        sender.sendMessage(BaseConstants.YELLOW + "You have toggled staff chat visibility " + (newChatToggled ? (ChatColor.GREEN + "on") : (ChatColor.RED + "off")) + BaseConstants.YELLOW + '.');
        return true;
    }
}
