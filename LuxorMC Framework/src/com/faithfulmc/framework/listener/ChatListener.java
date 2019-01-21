package com.faithfulmc.framework.listener;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.module.chat.StaffChatCommand;
import com.faithfulmc.framework.event.PlayerMessageEvent;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.ServerParticipator;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.Permissible;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatListener implements Listener {
    private static final String MESSAGE_SPY_FORMAT;
    private static final long AUTO_IDLE_TIME;
    private static final String STAFF_CHAT_NOTIFY = "base.command.staffchat";


    static {
        MESSAGE_SPY_FORMAT = BaseConstants.GOLD + "[" + ChatColor.DARK_RED + "SS: " + BaseConstants.YELLOW + "%1$s" + ChatColor.WHITE + " -> " + BaseConstants.YELLOW + "%2$s" + BaseConstants.GOLD + "] %3$s";
        AUTO_IDLE_TIME = TimeUnit.MINUTES.toMillis(5L);
    }

    private final BasePlugin plugin;

    public ChatListener(final BasePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        BaseUser baseUser = plugin.getUserManager().getUser(uuid);
        if (baseUser.isInStaffChat()) {
            final HashSet<CommandSender> remainingChatDisabled2 = Sets.newHashSet();
            for (final Permissible remainingChatSlowed : Bukkit.getOnlinePlayers()) {
                if (remainingChatSlowed.hasPermission(STAFF_CHAT_NOTIFY) && remainingChatSlowed instanceof CommandSender) {
                    remainingChatDisabled2.add((CommandSender) remainingChatSlowed);
                }
            }
            if (remainingChatDisabled2.contains(player) && baseUser.isInStaffChat()) {
                String format3 = StaffChatCommand.format(player.getName(), event.getMessage());
                BaseComponent[] global = TextComponent.fromLegacyText(format3);
                plugin.getGlobalMessager().broadcastToOtherServers(player, global, "base.command.staffchat");
                for (final CommandSender target2 : remainingChatDisabled2) {
                    if (target2 instanceof Player) {
                        Player speakTimeRemaining1 = (Player) target2;
                        BaseUser targetUser2 = this.plugin.getUserManager().getUser(speakTimeRemaining1.getUniqueId());
                        if (targetUser2.isStaffChatVisible()) {
                            target2.sendMessage(format3);
                        } else {
                            if (!target2.equals(player)) {
                                continue;
                            }
                            target2.sendMessage(ChatColor.RED + "Your message was sent, but you cannot see staff chat messages as your notifications are disabled: Use /togglesc.");
                        }
                    }
                }
                event.setCancelled(true);
                return;
            }
        }
        Iterator<?> iterator = event.getRecipients().iterator();
        while (iterator.hasNext()) {
            Player remainingChatDisabled = (Player) iterator.next();
            BaseUser format = this.plugin.getUserManager().getUser(remainingChatDisabled.getUniqueId());
            if(format == null){
                iterator.remove();
                continue;
            }
            if (baseUser.isInStaffChat() && !format.isStaffChatVisible()) {
                iterator.remove();
            } else if (format.getIgnoring().contains(player.getName())) {
                iterator.remove();
            } else {
                if (format.isGlobalChatVisible()) {
                    continue;
                }
                iterator.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPreMessage(final PlayerMessageEvent event) {
        final Player sender = event.getSender();
        final Player recipient = event.getRecipient();
        final UUID recipientUUID = recipient.getUniqueId();
        if (sender.hasPermission("base.messaging.bypass")) {
            final ServerParticipator senderParticipator1 = this.plugin.getUserManager().getParticipator((CommandSender) sender);
            if (!senderParticipator1.isMessagesVisible()) {
                event.setCancelled(true);
                sender.sendMessage(ChatColor.RED + "You have private messages toggled.");
            }
        } else {
            final BaseUser senderParticipator2 = this.plugin.getUserManager().getUser(recipientUUID);
            if (!senderParticipator2.isMessagesVisible() || senderParticipator2.getIgnoring().contains(sender.getName())) {
                event.setCancelled(true);
                sender.sendMessage(ChatColor.RED + recipient.getName() + " has private messaging toggled.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMessage(final PlayerMessageEvent event) {
        final Player sender = event.getSender();
        final Player recipient = event.getRecipient();
        final String message = event.getMessage();
        if (BukkitUtils.getIdleTime(recipient) > ChatListener.AUTO_IDLE_TIME) {
            sender.sendMessage(ChatColor.RED + recipient.getName() + " may not respond as their idle time is over " + DurationFormatUtils.formatDurationWords(ChatListener.AUTO_IDLE_TIME, true, true) + '.');
        }
        final UUID senderUUID = sender.getUniqueId();
        final String senderId = senderUUID.toString();
        final String recipientId = recipient.getUniqueId().toString();
        final HashSet<CommandSender> recipients = new HashSet<CommandSender>();
        recipients.addAll(Bukkit.getOnlinePlayers());
        recipients.remove(sender);
        recipients.remove(recipient);
        recipients.add(Bukkit.getConsoleSender());
        for (final CommandSender target : recipients) {
            final ServerParticipator participator = this.plugin.getUserManager().getParticipator(target);
            final Set<?> messageSpying = (Set<?>) participator.getMessageSpying();
            if (messageSpying.contains("all") || messageSpying.contains(recipientId) || messageSpying.contains(senderId)) {
                target.sendMessage(String.format(Locale.ENGLISH, ChatListener.MESSAGE_SPY_FORMAT, sender.getName(), recipient.getName(), message));
            }
        }
    }
}
