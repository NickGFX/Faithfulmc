package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.miner.MinerLevel;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class StatsResetCommand implements CommandExecutor {
    private final HCF hcf;

    public StatsResetCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length <= 0) {
            commandSender.sendMessage(ConfigurationService.RED + "Invalid syntax: " + ConfigurationService.YELLOW + "/" + s + " <player>");
        } else {
            UUID target = hcf.getUserManager().fetchUUID(args[0]);
            FactionUser factionUser;
            if(target == null || (factionUser = hcf.getUserManager().getUser(target)).getName() == null){
                commandSender.sendMessage(ConfigurationService.RED + "Player not found");
            }
            else  {
                factionUser.setKills(0);
                factionUser.setDeaths(0);
                factionUser.setKillStreak(0);
                factionUser.setBalance(250);
                factionUser.getMobs().clear();
                factionUser.getOres().clear();
                factionUser.setMinerLevel(MinerLevel.DEFAULT);
                if (factionUser.isOnline()) {
                    factionUser.getPlayer().sendMessage(ConfigurationService.YELLOW + "Your stats were reset by " + ConfigurationService.GOLD + commandSender.getName());
                }
                commandSender.sendMessage(ConfigurationService.RED + "Reset the stats of " + ConfigurationService.YELLOW + factionUser.getName());
            }
        }
        return true;
    }
}
