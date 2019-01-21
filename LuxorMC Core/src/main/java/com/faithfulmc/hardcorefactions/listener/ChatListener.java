package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.FactionChatEvent;
import com.faithfulmc.hardcorefactions.faction.struct.ChatChannel;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import net.minecraft.util.com.google.common.cache.CacheBuilder;
import net.minecraft.util.com.google.common.collect.HashMultimap;
import net.minecraft.util.org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    public static final List<String> blocked = Arrays.asList("ikari","cheatbreaker","cheat breaker","cheatbreaker","nigga", "kill yourself", "nigger", "prime", "pulsepvp", "para", "plugins", "plugin", "p4ra", "rip off", "shit staff", "shit server", "gay server", "kys", "leaked", "ddos", "velt", "hcteams", "minehq", "kill yourself", "viper", "oxpvp", "togglinq", "hydrahcf", "purgepots", "shit owner", "fuck this", "crap server", "crap staff", "bad staff", "gay staff", "etb", "exploitsquad", "arson", "lag", "botted", "fake players", "bot players", "my server", "join my server", "hcgames", "hcsquads", "server is shit", "server is crap", "server is so", "anticheat", "anti cheat", "bad server", "faggot", "anti-cheat", "hydra", "endyou", "whatspuberty", "kult", "this is shit", "staff", "mods", "arsonhcf", "spoofing", "playercount", "minehq", "restart", "#keyall", "#another","rollback","admins","stuck");
    private static final String DOUBLE_POST_BYPASS_PERMISSION = "hcf.doublepost.bypass";
    private static final String BLOCK_BYPASS_PERMISSION = "hcf.block.bypass";
    private static final Pattern PATTERN = Pattern.compile("\\W");
    private static final String SLOWED_CHAT_BYPASS = "base.slowchat.bypass";
    private static final String TOGGLED_CHAT_BYPASS = "base.disablechat.bypass";

    private final ConcurrentMap<Object, Object> messageHistory;
    private final HCF plugin;
    private final BasePlugin basePlugin = BasePlugin.getPlugin();
    private HashMultimap<UUID, Long> messages = HashMultimap.create();

    public ChatListener(HCF plugin) {
        this.plugin = plugin;
        this.messageHistory = CacheBuilder.newBuilder().expireAfterWrite(45, TimeUnit.SECONDS).build().asMap();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();
        String lastMessage = (String) this.messageHistory.get(player.getUniqueId());
        String cleanedMessage = PATTERN.matcher(message).replaceAll("");
        if ((lastMessage != null) && ((message.equals(lastMessage)) || (StringUtils.getLevenshteinDistance(cleanedMessage, lastMessage) <= 1)) && (!player.hasPermission(DOUBLE_POST_BYPASS_PERMISSION))) {
            player.sendMessage(ConfigurationService.RED + "Double posting is prohibited.");
            event.setCancelled(true);
            return;
        }
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        ChatChannel chatChannel = playerFaction == null ? ChatChannel.PUBLIC : playerFaction.getMember(player).getChatChannel();
        Set<Player> recipients = event.getRecipients();
        if ((chatChannel == ChatChannel.FACTION) || (chatChannel == ChatChannel.ALLIANCE)) {
            if (!isGlobalChannel(message)) {
                Collection<Player> online = playerFaction.getOnlinePlayers();
                if (chatChannel == ChatChannel.ALLIANCE) {
                    Collection<PlayerFaction> allies = playerFaction.getAlliedFactions();
                    for (PlayerFaction ally : allies) {
                        online.addAll(ally.getOnlinePlayers());
                    }
                }
                recipients.retainAll(online);
                event.setFormat(chatChannel.getRawFormat(player));
                Bukkit.getPluginManager().callEvent(new FactionChatEvent(true, playerFaction, player, chatChannel, recipients, event.getMessage()));
                return;
            }
            message = message.substring(1, message.length()).trim();
            event.setMessage(message);
        }
        event.setCancelled(true);
        if (playerFaction != null && playerFaction.isMuted()) {
            player.sendMessage(ConfigurationService.RED + "Your faction is muted" + (playerFaction.getMutetime() == -1 ? "" : " for " + ChatColor.BOLD + DurationFormatUtils.formatDurationWords(playerFaction.getMutetime() - System.currentTimeMillis(), true, true)));
            return;
        }
        BaseUser baseUser = this.basePlugin.getUserManager().getUser(player.getUniqueId());
        final long remainingChatDisabled3 = this.basePlugin.getServerHandler().getRemainingChatDisabledMillis();
        if (remainingChatDisabled3 > 0L && !player.hasPermission(TOGGLED_CHAT_BYPASS)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Global chat is currently disabled for " + BaseConstants.GRAY + DurationFormatUtils.formatDurationWords(remainingChatDisabled3, true, true) + ChatColor.RED + '.');
            return;
        }
        final long remainingChatSlowed3 = this.basePlugin.getServerHandler().getRemainingChatSlowedMillis();
        if (remainingChatSlowed3 > 0L && !player.hasPermission(SLOWED_CHAT_BYPASS)) {
            final long speakTimeRemaining2 = baseUser.getLastSpeakTimeRemaining();
            if (speakTimeRemaining2 <= 0L) {
                baseUser.updateLastSpeakTime();
            }
            else {
                event.setCancelled(true);
                final long delayMillis = this.basePlugin.getServerHandler().getChatSlowedDelay() * 1000L;
                player.sendMessage(ChatColor.RED + "Global chat is currently in slow mode for " + BaseConstants.GRAY + DurationFormatUtils.formatDurationWords(remainingChatSlowed3, true, true) + ChatColor.RED + ". You spoke " + BaseConstants.GRAY + DurationFormatUtils.formatDurationWords(delayMillis - speakTimeRemaining2, true, true) + ChatColor.RED + " ago, so you must wait another " + BaseConstants.GRAY + DurationFormatUtils.formatDurationWords(speakTimeRemaining2, true, true) + ChatColor.RED + '.');
                return;
            }
        }
        String rank = ChatColor.translateAlternateColorCodes('&', "&e" + HCF.getChat().getPlayerPrefix(player).replace("_", " "));
        String displayName = player.getDisplayName();
        displayName = rank + displayName;
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        boolean block = false;
        if(!player.hasPermission(BLOCK_BYPASS_PERMISSION)) {
            for (String word : blocked) {
                if (message.toLowerCase().contains(word)) {
                    block = true;
                    break;
                }
            }
        }
        if (message.length() == 1 && !(message.equalsIgnoreCase("o") || message.equalsIgnoreCase("k") || message.equalsIgnoreCase("?") || message.equalsIgnoreCase("l"))) {
            event.getRecipients().clear();
            event.getRecipients().add(player);
        } else if(!player.hasPermission(BLOCK_BYPASS_PERMISSION)){
            long now = System.currentTimeMillis();
            messages.put(player.getUniqueId(), System.currentTimeMillis());
            int amount = 0;
            for (long timestamp : new HashSet<>(messages.get(player.getUniqueId()))) {
                if (now - 5000 > timestamp) {
                    messages.remove(player.getUniqueId(), timestamp);
                } else {
                    amount++;
                }
            }
            if (amount >= 5) {
                if (amount == 5) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("base.command.staffchat")) {
                            p.sendMessage(ChatColor.DARK_RED + "[!] " + ConfigurationService.RED + player.getName() + ConfigurationService.GRAY + " was stopped from spamming");
                        }
                    }
                }
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute " + player.getName() + " -s 5m Spam"));
                event.getRecipients().clear();
                event.getRecipients().add(player);
            } else if (amount >= 3) {
                block = true;
            }
        }
        if (HCF.getChat() == null) {
            event.setCancelled(true);
            return;
        }
        this.messageHistory.put(player.getUniqueId(), cleanedMessage);
        String tag = playerFaction == null ? ChatColor.RED + "*" : playerFaction.getDisplayName(console);

        String arrow = (player.hasPermission("hcf.silverarrow") ? ChatColor.DARK_GRAY : ChatColor.GOLD) + ConfigurationService.DOUBLEARROW;
        String blockedarrow = ChatColor.RED + ConfigurationService.DOUBLEARROW;

        String chatColor = baseUser.getChatColor() == null ? ChatColor.WHITE.toString() : baseUser.getChatColor().toString();

        console.sendMessage(ConfigurationService.ARROW_COLOR + "[" + tag + ConfigurationService.ARROW_COLOR + "] " + displayName + " " + arrow + " " + ConfigurationService.GRAY + message);

        for (Player recipient : event.getRecipients()) {
            tag = playerFaction == null ? ChatColor.RED + "*" : playerFaction.getDisplayName(recipient);
            if (block) {
                if (recipient == player) {
                    recipient.sendMessage(ConfigurationService.ARROW_COLOR + "[" + tag + ConfigurationService.ARROW_COLOR + "] " + displayName + " " + arrow + " " + chatColor + message);
                } else if (recipient.hasPermission("base.command.staffchat")) {
                    recipient.sendMessage(ConfigurationService.ARROW_COLOR + "[" + tag + ConfigurationService.ARROW_COLOR + "] " + displayName + " " + blockedarrow + " " + chatColor + message);
                }
            } else {
                recipient.sendMessage(ConfigurationService.ARROW_COLOR + "[" + tag + ConfigurationService.ARROW_COLOR + "] " + displayName + " " + arrow + " " + chatColor + message);
            }
        }
    }

    private boolean isGlobalChannel(String input) {
        int length = input.length();
        if ((length <= 1) || (!input.startsWith("!"))) {
            return false;
        }
        int i = 1;
        while (i < length) {
            char character = input.charAt(i);
            if (character == ' ') {
                i++;
            } else {
                if (character != '/') {
                    break;
                }
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        messages.asMap().remove(event.getPlayer().getUniqueId());
    }
}
