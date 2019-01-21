package com.faithfulmc.hardcorefactions.command.lives.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.deathban.Deathban;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.util.DateTimeFormats;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LivesDeathBanHistoryArgument extends CommandArgument {
    private final HCF plugin;

    public LivesDeathBanHistoryArgument(HCF plugin) {
        super("checkdeathbanhistory", "Check the deathban cause of player");
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
            if(target == null || (factionUser = plugin.getUserManager().getUser(target)).getName() == null){
                sender.sendMessage(ConfigurationService.RED + "Player not found");
            }
            else {
                List<Deathban> deathbanList = factionUser.getDeathbanHistory();
                int i = deathbanList.size();
                for (Deathban deathban : deathbanList) {
                    sender.sendMessage(ConfigurationService.RED.toString() + i + ")");
                    sender.sendMessage(ConfigurationService.YELLOW + " Time: " + ConfigurationService.GRAY + DateTimeFormats.HR_MIN.format(deathban.getCreationMillis()));
                    Location location = deathban.getDeathPoint();
                    if (location != null) {
                        sender.sendMessage(ConfigurationService.YELLOW + " Location: " + ConfigurationService.GRAY + "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ") - " + location.getWorld().getName());
                    }
                    sender.sendMessage(ConfigurationService.YELLOW + " Reason: " + ConfigurationService.GRAY + Strings.nullToEmpty(deathban.getReason()));
                    i--;
                }
            }
        }
        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            List<String> results = new ArrayList<>();
            Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (senderPlayer == null || senderPlayer.canSee(target)) {
                    results.add(target.getName());
                }
            }
            return BukkitUtils.getCompletions(args, results);
        }
        return Collections.emptyList();
    }

}
