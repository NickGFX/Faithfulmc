package com.faithfulmc.hardcorefactions.command.spawncredit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class SpawnCreditGiveArgument extends CommandArgument{
    private final HCF hcf;

    public SpawnCreditGiveArgument(HCF hcf) {
        super("give", "Gives a player spawn credits", "hcf.command.spawncredit.argument.give");
        this.hcf = hcf;
    }

    public String getUsage(String label) {
        return "/" + label + " " + getName() + " <player> <amount>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ConfigurationService.YELLOW + "Invalid args: " + ConfigurationService.GRAY + getUsage(label));
        } else {
            UUID target = hcf.getUserManager().fetchUUID(args[1]);
            FactionUser factionUser;
            if (target == null || (factionUser = hcf.getUserManager().getUser(target)).getName() == null) {
                sender.sendMessage(ConfigurationService.RED + "Player not found");
            } else {
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ConfigurationService.RED + "Invalid number");
                    return true;
                }
                if (amount <= 0) {
                    sender.sendMessage(ConfigurationService.RED + "Invalid number");
                } else {
                    factionUser.setSpawncredits(factionUser.getSpawncredits() + amount);
                    sender.sendMessage(ConfigurationService.YELLOW + "Gave " + ConfigurationService.GOLD + factionUser.getName() + " " + ConfigurationService.GRAY + amount + ConfigurationService.YELLOW + " spawn credit" + (amount != 1 ? "s" : ""));
                    if (factionUser.isOnline()) {
                        factionUser.getPlayer().sendMessage(ConfigurationService.YELLOW + "You now have " + ConfigurationService.GRAY + factionUser.getSpawncredits() + ConfigurationService.YELLOW + " spawn credits");
                    }
                }
            }
        }
        return true;
    }
}
