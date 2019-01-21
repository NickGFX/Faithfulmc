package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class SetReclaimedCommand implements CommandExecutor{
    private final HCF hcf;

    public SetReclaimedCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length != 2){
            sender.sendMessage(ConfigurationService.YELLOW + "Invalid args: " + ConfigurationService.GRAY + "/" + label + " <target> <true/false>");
        }
        else {
            boolean b;
            try {
                b = Boolean.parseBoolean(args[1]);
            } catch (Exception e) {
                sender.sendMessage(ConfigurationService.YELLOW + "Invalid args: " + ConfigurationService.GRAY + "/" + label + " <target> <true/false>");
                return true;
            }
            UUID target = hcf.getUserManager().fetchUUID(args[0]);
            FactionUser factionUser;
            if (target == null || (factionUser = hcf.getUserManager().getUser(target)).getName() == null) {
                sender.sendMessage(ConfigurationService.RED + "Player has not joined before");
            } else {
                factionUser.setReclaimed(b);
                sender.sendMessage(ConfigurationService.YELLOW + factionUser.getName() + " reclaim set to " + ConfigurationService.GRAY + String.valueOf(b));
            }
        }
        return true;
    }
}
