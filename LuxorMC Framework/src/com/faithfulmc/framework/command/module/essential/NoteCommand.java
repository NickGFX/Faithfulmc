package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

public class NoteCommand extends BaseCommand {
    public NoteCommand() {
        super("note", "add, removes, and checks notes for a framework user");
        this.setUsage("/(command) <add|remove|check> <playerName> [note]");
        this.setAliases(new String[]{"addnote, notes, checknote, removenote"});
    }

    @Override
    public boolean onCommand(final CommandSender cs, final Command cmd, final String s, final String[] args) {
        new BukkitRunnable() {
            public void run() {
                if (!(cs instanceof Player)) {
                    cs.sendMessage(ChatColor.RED + "Please use the server to execute this command.");
                    return;
                }
                final Player player = (Player) cs;
                if (args.length < 2) {
                    player.sendMessage(getUsage(s));
                    return;
                }
                if (Bukkit.getPlayer(args[1]) == null && Bukkit.getOfflinePlayer(args[1]) == null) {
                    cs.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
                    return;
                }
                final OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                final BaseUser targetUser = BasePlugin.getPlugin().getUserManager().getUser(target.getUniqueId());
                final String note = StringUtils.join((Object[]) args, ' ', 2, args.length);
                if (args[0].equalsIgnoreCase("add")) {
                    final String upTime = DateFormatUtils.format(System.currentTimeMillis(), "MM/dd/yy");
                    final String time = DateFormatUtils.format(System.currentTimeMillis(), "hh:mm");
                    targetUser.setNote(BaseConstants.GOLD + cs.getName() + BaseConstants.GRAY + " [" + upTime + " | " + time + "]" + BaseConstants.YELLOW + " - " + note);
                    player.sendMessage(BaseConstants.YELLOW + "You added a note to " + targetUser.getName());
                    return;
                }
                if (args[0].equalsIgnoreCase("remove")) {
                    if (!player.hasPermission(getPermission() + ".remove")) {
                        player.sendMessage(ChatColor.RED + "No permission to this argument.");
                        return;
                    }
                    if (targetUser.tryRemoveNote()) {
                        Command.broadcastCommandMessage(cs, BaseConstants.YELLOW + "Removed note of " + target.getName() + BaseConstants.YELLOW + '.');
                        return;
                    }
                    player.sendMessage(ChatColor.RED + "Note not found or other error.");
                    return;
                } else {
                    if (args[0].equalsIgnoreCase("check")) {
                        player.sendMessage(BaseConstants.YELLOW + "Notes: ");
                        for (final String notes : targetUser.getNotes()) {
                            player.sendMessage(notes);
                        }
                        return;
                    }
                    player.sendMessage(getUsage(s));
                    return;
                }
            }
        }.runTaskAsynchronously(BasePlugin.getPlugin());
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 2 && sender.hasPermission(command.getPermission())) ? null : Collections.emptyList();
    }
}
