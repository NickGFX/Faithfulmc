package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.ChatChannel;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FactionChatArgument extends CommandArgument {
    private final HCF plugin;

    public FactionChatArgument(HCF plugin) {
        super("chat", "Toggle faction chat only mode on or off.", new String[]{"c"});
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName() + " [fac|public|ally] [message]";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
        } else {
            Player player = (Player) sender;
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            } else {
                FactionMember member = playerFaction.getMember(player.getUniqueId());
                ChatChannel currentChannel = member.getChatChannel();
                ChatChannel parsed = args.length >= 2 ? ChatChannel.parse(args[1], null) : currentChannel.getRotation();
                if (parsed == null && currentChannel != ChatChannel.PUBLIC) {
                    Set<Player> recipients = playerFaction.getOnlinePlayers();
                    if (currentChannel == ChatChannel.ALLIANCE) {
                        for (PlayerFaction ally : playerFaction.getAlliedFactions()) {
                            recipients.addAll(ally.getOnlinePlayers());
                        }
                    }
                    String format = String.format(currentChannel.getRawFormat(player), "", StringUtils.join(args, ' ', 1, args.length));
                    for (Player recipient : recipients) {
                        recipient.sendMessage(format);
                    }
                } else {
                    ChatChannel newChannel = parsed == null ? currentChannel.getRotation() : parsed;
                    member.setChatChannel(newChannel);
                    sender.sendMessage(ConfigurationService.YELLOW + "You are now in " + ConfigurationService.GRAY + newChannel.getDisplayName().toLowerCase() + ConfigurationService.YELLOW + " chat mode.");
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }
        ChatChannel[] values = ChatChannel.values();
        List<String> results = new ArrayList<String>(values.length);
        for (ChatChannel type : values) {
            results.add(type.getName());
        }
        return results;
    }
}