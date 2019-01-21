package com.faithfulmc.hardcorefactions.command.lives.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.deathban.Deathban;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.util.DateTimeFormats;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.base.Strings;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class LivesCheckDeathbanArgument extends CommandArgument {
    private final HCF plugin;


    public LivesCheckDeathbanArgument(HCF plugin) {

        super("checkdeathban", "Check the deathban cause of player");

        this.plugin = plugin;

        this.permission = ("hcf.command.lives.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <playerName>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            UUID target = plugin.getUserManager().fetchUUID(args[1]);
            FactionUser factionUser;
            if (target == null || (factionUser = plugin.getUserManager().getUser(target)).getName() == null) {
                sender.sendMessage(ConfigurationService.RED + "Player not found");
            } else {
                Deathban deathban = factionUser.getDeathban();
                if (deathban == null) {
                    sender.sendMessage(ConfigurationService.RED + factionUser.getName() + " is not death-banned.");
                } else {
                    sender.sendMessage(ConfigurationService.GOLD + "Deathban cause of " + factionUser.getName() + '.');
                    sender.sendMessage(ConfigurationService.YELLOW + " Time: " + ConfigurationService.GRAY + DateTimeFormats.HR_MIN.format(deathban.getCreationMillis()));
                    sender.sendMessage(ConfigurationService.YELLOW + " Duration: " + ConfigurationService.GRAY + DurationFormatUtils.formatDurationWords(deathban.getExpiryMillis() - deathban.getCreationMillis(), true, true));
                    Location location = deathban.getDeathPoint();
                    if (location != null) {
                        sender.sendMessage(ConfigurationService.YELLOW + " Location: " + ConfigurationService.GRAY + "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ") - " + location.getWorld().getName());
                    }
                    sender.sendMessage(ConfigurationService.YELLOW + " Reason: " + ConfigurationService.GRAY + Strings.nullToEmpty(deathban.getReason()));
                    if (!deathban.isActive()) {
                        sender.sendMessage(ConfigurationService.GRAY + "User deathban has expired");
                    }
                }
            }
        }
        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        for (FactionUser factionUser : this.plugin.getUserManager().getUsers().values()) {
            Deathban deathban = factionUser.getDeathban();
            if (deathban != null && deathban.isActive()) {
                String name = factionUser.getName();
                if (name != null) {
                    results.add(name);
                }
            }
        }

        return results;

    }

}