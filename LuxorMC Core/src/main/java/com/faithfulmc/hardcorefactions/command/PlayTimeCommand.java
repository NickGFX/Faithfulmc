package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayTimeCommand extends BaseCommand {
    private final HCF plugin;

    public PlayTimeCommand(final HCF plugin) {
        super("playtime", "Check the playtime of another player.");
        this.setAliases(new String[]{"pt", "bb"});
        this.setUsage("/(command) [playerName]");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        FactionUser factionUser;
        if(args.length >= 1 && !ConfigurationService.ORIGINS) {
            if(sender instanceof Player && args[0].equalsIgnoreCase("claim")){
                Player player = (Player) sender;
                factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                long lastClaim = factionUser.getLastPlaytimeReclaim();
                long currentPlaytime = factionUser.getCurrentPlaytime();
                if(currentPlaytime - lastClaim > TimeUnit.MINUTES.toMillis(ConfigurationService.PLAYTIME_RECLAIM_MINUTES)){
                    for(String executedCommand: ConfigurationService.PLAYTIME_RECLAIM_COMMANDS){
                        factionUser.setLastPlaytimeReclaim(currentPlaytime);
                        executedCommand = executedCommand.replace("%player%", player.getName());
                        if(executedCommand.startsWith("/")){
                            executedCommand = executedCommand.substring(1);
                        }
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executedCommand);
                    }
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                    player.sendMessage(ConfigurationService.YELLOW + "Successfully reclaimed your playtime reward");
                }
                else{
                    long timeTill = TimeUnit.MINUTES.toMillis(ConfigurationService.PLAYTIME_RECLAIM_MINUTES) - (currentPlaytime - lastClaim);
                    player.sendMessage(ConfigurationService.YELLOW + "You must be connected to " + ConfigurationService.GOLD + BasePlugin.getPlugin().getGlobalMessager().getId() + ConfigurationService.YELLOW + " for another " + ChatColor.AQUA + DurationFormatUtils.formatDurationWords(timeTill, true, true) + ConfigurationService.YELLOW + " before reclaiming your reward.");
                }
                return true;
            }
            else {
                UUID target = plugin.getUserManager().fetchUUID(args[0]);
                if (target == null || (factionUser = plugin.getUserManager().getUser(target)).getName() == null) {
                    sender.sendMessage(ConfigurationService.RED + "Player has not joined before");
                    return true;
                }
            }
        }
        else if(sender instanceof Player){
            factionUser = plugin.getUserManager().getUser(((Player)sender).getUniqueId());
            if(!ConfigurationService.ORIGINS) {
                long lastClaim = factionUser.getLastPlaytimeReclaim();
                long currentPlaytime = factionUser.getCurrentPlaytime();
                if (currentPlaytime - lastClaim > TimeUnit.MINUTES.toMillis(ConfigurationService.PLAYTIME_RECLAIM_MINUTES)) {
                    sender.sendMessage(ConfigurationService.YELLOW + "You may use " + ConfigurationService.GRAY + "/pt claim" + ConfigurationService.YELLOW + " in order to claim your playtime reward.");
                }
            }
        }
        else{
            sender.sendMessage(ChatColor.RED + getUsage(label));
            return true;
        }
        sender.sendMessage(ConfigurationService.GOLD + factionUser.getName() + ConfigurationService.YELLOW + " has been playing for " + ConfigurationService.GRAY + DurationFormatUtils.formatDurationWords(factionUser.getCurrentPlaytime(), true, true) + ConfigurationService.YELLOW + " this map.");
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }
}
