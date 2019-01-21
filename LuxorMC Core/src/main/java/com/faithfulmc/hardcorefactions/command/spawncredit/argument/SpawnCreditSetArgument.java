package com.faithfulmc.hardcorefactions.command.spawncredit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class SpawnCreditSetArgument extends CommandArgument{
    private final HCF hcf;

    public SpawnCreditSetArgument(HCF hcf) {
        super("set", "Sets the amount of spawn credits a player has", "hcf.command.spawncredit.argument.set");
        this.hcf = hcf;
    }

    public String getUsage(String label) {
        return "/" + label + " " + getName() + " <player> <amount>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 3){
            sender.sendMessage(ConfigurationService.YELLOW + "Invalid args: " + ConfigurationService.GRAY + getUsage(label));
        }
        else{
            UUID target = hcf.getUserManager().fetchUUID(args[1]);
            FactionUser factionUser;
            if(target == null || (factionUser = hcf.getUserManager().getUser(target)).getName() == null){
                sender.sendMessage(ConfigurationService.RED + "Player not found");
            }
            else{
                int amount;
                try{
                    amount = Integer.parseInt(args[2]);
                }
                catch (NumberFormatException ex){
                    sender.sendMessage(ConfigurationService.RED + "Invalid number");
                    return true;
                }
                if(amount < 0){
                    sender.sendMessage(ConfigurationService.RED + "Invalid number");
                }
                else{
                    factionUser.setSpawncredits(amount);
                    sender.sendMessage(ConfigurationService.YELLOW + "Set the credits of " + ConfigurationService.GOLD + factionUser.getName() + ConfigurationService.YELLOW + " to " + ConfigurationService.GRAY + amount);
                    if(factionUser.isOnline()){
                        factionUser.getPlayer().sendMessage(ConfigurationService.YELLOW + "You now have " + ConfigurationService.GRAY + amount + ConfigurationService.YELLOW + " spawn credits");
                    }
                }
            }
        }
        return true;
    }
}
