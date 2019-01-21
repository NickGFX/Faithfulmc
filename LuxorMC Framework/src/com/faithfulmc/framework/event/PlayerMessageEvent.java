package com.faithfulmc.framework.event;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.user.BaseUser;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;

public class PlayerMessageEvent extends Event implements Cancellable {
    private static final HandlerList handlers;

    public static HandlerList getHandlerList() {
        return PlayerMessageEvent.handlers;
    }

    static {
        handlers = new HandlerList();
    }

    private final Player sender;
    private final Player recipient;
    private final String message;
    private final boolean isReply;
    private boolean cancelled;

    public PlayerMessageEvent(final Player sender, final Set recipients, final String message, final boolean isReply) {
        super(true);
        this.cancelled = false;
        this.sender = sender;
        this.recipient = (Player) Iterables.getFirst((Iterable) recipients, (Object) null);
        this.message = message;
        this.isReply = isReply;
    }

    public Player getSender() {
        return this.sender;
    }

    public Player getRecipient() {
        return this.recipient;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isReply() {
        return this.isReply;
    }

    public void send() {
        Preconditions.checkNotNull((Object) this.sender, (Object) "The sender cannot be null");
        Preconditions.checkNotNull((Object) this.recipient, (Object) "The recipient cannot be null");
        final BasePlugin plugin = BasePlugin.getPlugin();
        final BaseUser sendingUser = plugin.getUserManager().getUser(this.sender.getUniqueId());
        final BaseUser recipientUser = plugin.getUserManager().getUser(this.recipient.getUniqueId());
        sendingUser.setLastRepliedTo(recipientUser.getUniqueId());
        recipientUser.setLastRepliedTo(sendingUser.getUniqueId());
        final long millis = System.currentTimeMillis();
        recipientUser.setLastReceivedMessageMillis(millis);
        final String rank = ChatColor.translateAlternateColorCodes('&', "&f" + BasePlugin.getChat().getPlayerPrefix(sender)).replace("_", " ");
        final String displayName = rank + this.sender.getDisplayName();
        final String rank2 = ChatColor.translateAlternateColorCodes('&', "&f" + BasePlugin.getChat().getPlayerPrefix(recipient)).replace("_", " ");
        final String displayName2 = rank2 + this.recipient.getDisplayName();
        if(recipientUser.isMessagingSounds()){
            recipient.playSound(recipient.getLocation(), Sound.NOTE_PLING, 1f, 1f);
        }
        this.sender.sendMessage(ChatColor.GRAY + "(To " + displayName2 + ChatColor.GRAY + ") " + ChatColor.GRAY + this.message);
        this.recipient.sendMessage(ChatColor.GRAY + "(From " + displayName + ChatColor.GRAY + ") " + ChatColor.GRAY + this.message);
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return PlayerMessageEvent.handlers;
    }
}
