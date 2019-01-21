package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.event.PlayerMessageEvent;
import com.faithfulmc.framework.user.BaseUser;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReplyCommand extends BaseCommand {
    private static final long VANISH_REPLY_TIMEOUT;

    static {
        VANISH_REPLY_TIMEOUT = TimeUnit.SECONDS.toMillis(45L);
    }

    private final BasePlugin plugin;

    public ReplyCommand(final BasePlugin plugin) {
        super("reply", "Replies to the last conversing player.");
        this.setAliases(new String[]{"r", "respond"});
        this.setUsage("/(command) <message>");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new BukkitRunnable() {
            public void run() {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command is only executable for players.");
                    return;
                }
                final Player player = (Player) sender;
                final UUID uuid = player.getUniqueId();
                final BaseUser baseUser = plugin.getUserManager().getUser(uuid);
                final UUID lastReplied = baseUser.getLastRepliedTo();
                final Player target = (lastReplied == null) ? null : Bukkit.getPlayer(lastReplied);
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
                    if (lastReplied != null && BaseCommand.canSee(sender, target)) {
                        sender.sendMessage(ChatColor.RED + "You are in a conversation with " + target.getName() + '.');
                    }
                    return;
                }
                final long millis = System.currentTimeMillis();
                if (target == null || (!BaseCommand.canSee(sender, target) && millis - baseUser.getLastReceivedMessageMillis() > ReplyCommand.VANISH_REPLY_TIMEOUT)) {
                    sender.sendMessage(BaseConstants.GOLD + "There is no player to reply to.");
                    return;
                }
                final String message = StringUtils.join((Object[]) args, ' ', 0, args.length);
                final HashSet recipients = Sets.newHashSet((Object[]) new Player[]{target});
                final PlayerMessageEvent playerMessageEvent = new PlayerMessageEvent(player, recipients, message, false);
                Bukkit.getPluginManager().callEvent((Event) playerMessageEvent);
                if (!playerMessageEvent.isCancelled()) {
                    playerMessageEvent.send();
                }
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return null;
    }
}
