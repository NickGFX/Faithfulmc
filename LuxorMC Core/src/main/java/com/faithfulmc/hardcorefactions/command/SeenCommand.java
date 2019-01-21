package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.util.org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class SeenCommand implements CommandExecutor {
    private final HCF plugin;

    public SeenCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ConfigurationService.RED + "Invalid args: " + ConfigurationService.YELLOW + "/seen <player>");
        } else {
            UUID target = plugin.getUserManager().fetchUUID(args[0]);
            FactionUser factionUser;
            if (target == null || (factionUser = plugin.getUserManager().getUser(target)).getName() == null) {
                sender.sendMessage(ConfigurationService.RED + "Player not found");
            } else {
                long lastSeen = factionUser.getLastSeen();
                if (lastSeen <= 0) {
                    sender.sendMessage(ConfigurationService.RED + "That player has not joined before");
                } else {
                    long now = System.currentTimeMillis();
                    boolean online = factionUser.isOnline();
                    long time = now - lastSeen;
                    sender.sendMessage(ConfigurationService.GOLD + factionUser.getName() + ConfigurationService.GRAY + " has been " + (online ? ChatColor.GREEN + "online" : ConfigurationService.RED + "offline") + ConfigurationService.GRAY + " for " + ConfigurationService.YELLOW + DurationFormatUtils.formatDurationWords(time, true, true));
                }
            }
        }
        return true;
    }
}
