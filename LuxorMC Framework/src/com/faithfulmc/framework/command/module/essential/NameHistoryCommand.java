package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.util.NameHistory;
import org.apache.commons.lang3.time.FastDateFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class NameHistoryCommand extends BaseCommand {
    private static final FastDateFormat FORMAT;

    static {
        FORMAT = FastDateFormat.getInstance("EEE, MMM d yy, hh:mmaaa", Locale.ENGLISH);
    }

    private final BasePlugin plugin;

    public NameHistoryCommand(final BasePlugin plugin) {
        super("namehistory", "Checks name change histories of players.");
        this.setUsage("/(command) <player>");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        if(BasePlugin.isMongo()){
            return true;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Pattern pattern = Pattern.compile("^" + args[0] + "$", Pattern.CASE_INSENSITIVE);
            BaseUser targetUser = this.plugin.getDatastore().find(BaseUser.class).field("name").equal(pattern).project("nameHistories", true).get();
            if (targetUser == null) {
                sender.sendMessage(BaseConstants.GOLD + "Player '" + ChatColor.WHITE + args[0] + BaseConstants.GOLD + "' not found.");
            } else {
                for (NameHistory nameHistory : targetUser.getNameHistories()) {
                    sender.sendMessage(BaseConstants.GRAY + targetUser.getName() + " (" + NameHistoryCommand.FORMAT.format(nameHistory.getMillis()) + ')');
                }
            }
        });
        return true;
    }

    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }
}
