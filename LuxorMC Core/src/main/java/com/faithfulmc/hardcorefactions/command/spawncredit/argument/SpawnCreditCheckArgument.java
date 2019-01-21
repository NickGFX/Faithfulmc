package com.faithfulmc.hardcorefactions.command.spawncredit.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SpawnCreditCheckArgument extends CommandArgument{
    private final HCF hcf;

    public SpawnCreditCheckArgument(HCF hcf) {
        super("check", "Check how many spawn credits you have", null, new String[]{"get","view","amount"});
        this.hcf = hcf;
    }

    public String getUsage(String label) {
        return "/" + label + " "  + getName() + " [player]";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1){
            if(sender instanceof Player){
                Player player = (Player)sender;
                FactionUser factionUser = hcf.getUserManager().getUser(player.getUniqueId());
                sender.sendMessage(ConfigurationService.YELLOW + "You currently have " + ConfigurationService.GRAY + factionUser.getSpawncredits() + ConfigurationService.YELLOW + " spawn credits");
                sender.sendMessage(ConfigurationService.YELLOW + "To get more visit " + ConfigurationService.GOLD + ConfigurationService.STORE);
            }
            else{
                sender.sendMessage(ConfigurationService.RED + "You need to be a player to do this");
            }
        }
        else if(args.length == 2){
            UUID target = hcf.getUserManager().fetchUUID(args[1]);
            FactionUser factionUser;
            if(target == null || (factionUser = hcf.getUserManager().getUser(target)).getName() == null){
                sender.sendMessage(ConfigurationService.RED + "Player not found");
            }
            else{
                sender.sendMessage(ConfigurationService.YELLOW + factionUser.getName() + " current has " + ConfigurationService.GRAY + factionUser.getSpawncredits() + ConfigurationService.YELLOW + " spawn credits");
            }
        }
        else{
            sender.sendMessage(getUsage(label));
        }
        return true;
    }
}
