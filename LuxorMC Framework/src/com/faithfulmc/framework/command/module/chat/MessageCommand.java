package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.event.PlayerMessageEvent;
import com.faithfulmc.util.BukkitUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MessageCommand extends BaseCommand {
    private final BasePlugin plugin;

    public MessageCommand(final BasePlugin plugin) {
        super("message", "Sends a message to a recipient(s).");
        this.plugin = plugin;
        this.setAliases(new String[]{"msg", "m", "whisper", "w", "tell"});
        this.setUsage("/(command) <playerName> [text...]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new BukkitRunnable() {
            public void run() {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command is only executable for players.");
                    return;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
                    return;
                }
                final Player player = (Player) sender;
                final Player target = BukkitUtils.playerWithNameOrUUID(args[0]);
                if (target != null && BaseCommand.canSee(sender, target)) {
                    final String message = StringUtils.join((Object[]) args, ' ', 1, args.length);
                    final Set recipients = Collections.singleton(target);
                    final PlayerMessageEvent playerMessageEvent = new PlayerMessageEvent(player, recipients, message, false);
                    Bukkit.getPluginManager().callEvent((Event) playerMessageEvent);
                    if (!playerMessageEvent.isCancelled()) {
                        playerMessageEvent.send();
                    }
                    return;
                }
                sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return null;
    }
}
