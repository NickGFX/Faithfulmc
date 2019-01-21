package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.RegenStatus;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;


public class RegenCommand implements CommandExecutor, TabCompleter {
    private final HCF plugin;


    public RegenCommand(HCF plugin) {

        this.plugin = plugin;

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");

            return true;

        }

        Player player = (Player) sender;

        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {

            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");

            return true;

        }

        RegenStatus regenStatus = playerFaction.getRegenStatus();

        switch (regenStatus) {

            case FULL:

                sender.sendMessage(ConfigurationService.RED + "Your faction currently has full DTR.");

                return true;

            case PAUSED:

                sender.sendMessage(ChatColor.BLUE + "Your faction is currently on DTR freeze for another " + ConfigurationService.GOLD + DurationFormatUtils.formatDurationWords(playerFaction.getRemainingRegenerationTime(), true, true) + ChatColor.BLUE + '.');
                return true;

            case REGENERATING:
                sender.sendMessage(ChatColor.BLUE + "Your faction currently has " + ConfigurationService.YELLOW + regenStatus.getSymbol() + ' ' + playerFaction.getDeathsUntilRaidable() + ChatColor.BLUE + " DTR and is regenerating at a rate of " + ConfigurationService.GOLD + "0.1" + ChatColor.BLUE + " every " + ConfigurationService.GOLD + ConfigurationService.DTR_WORDS_BETWEEN_UPDATES + ChatColor.BLUE + ". Your ETA for maximum DTR is " + ChatColor.LIGHT_PURPLE + DurationFormatUtils.formatDurationWords(getRemainingRegenMillis(playerFaction), true, true) + ChatColor.BLUE + '.');
                return true;

        }
        sender.sendMessage(ConfigurationService.RED + "Unrecognised regen status, please inform an Administrator.");
        return true;

    }


    public long getRemainingRegenMillis(PlayerFaction faction) {
        long millisPassedSinceLastUpdate = System.currentTimeMillis() - faction.getLastDtrUpdateTimestamp();
        double dtrRequired = faction.getMaximumDeathsUntilRaidable() - faction.getDeathsUntilRaidable();
        return (long) (((float) ConfigurationService.DTR_MILLIS_BETWEEN_UPDATES / 0.1F * dtrRequired) - millisPassedSinceLastUpdate);
    }


    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();

    }

}