package com.faithfulmc.hardcorefactions.command.lives.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.primitives.Ints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class LivesGiveArgument extends CommandArgument {
    private final HCF plugin;

    public LivesGiveArgument(HCF plugin) {
        super("give", "Give lives to a player");
        this.plugin = plugin;
        this.aliases = new String[]{"transfer", "send", "pay", "add"};
        this.permission = ("hcf.command.lives.argument." + getName());

    }


    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName> <amount>";
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            Integer amount = Ints.tryParse(args[2]);
            if (amount == null) {
                sender.sendMessage(ConfigurationService.RED + "'" + args[2] + "' is not a number.");
            }
            else if (amount <= 0) {
                sender.sendMessage(ConfigurationService.RED + "The amount of lives must be positive.");
            }
            else {
                Player senderPlayer = sender instanceof Player ? (Player) sender : null;
                FactionUser factionUser = senderPlayer == null ? null : plugin.getUserManager().getUser(senderPlayer.getUniqueId());
                if(factionUser != null && amount > factionUser.getLives()){
                    sender.sendMessage(ConfigurationService.RED + "You only have " + factionUser.getLives() + '.');
                }
                else if(args[1].equalsIgnoreCase("*") && sender.hasPermission(getName() + ".all")){
                    sender.sendMessage(ConfigurationService.YELLOW + "You have sent " + ConfigurationService.GOLD + "all online players" + ConfigurationService.YELLOW + " " + amount + " " + ConfigurationService.YELLOW + (amount == 1 ? "life" : "lives") + ".");
                    for(FactionUser targetUser: plugin.getUserManager().getOnlineStorage().values()){
                        targetUser.setLives(targetUser.getLives() + amount);
                        sender.sendMessage(ConfigurationService.YELLOW + "You have sent " + ConfigurationService.GOLD + targetUser.getName() + ConfigurationService.YELLOW + " " + amount + " " + ConfigurationService.YELLOW + (amount == 1 ? "life" : "lives") + ".");
                        if (targetUser.isOnline()) {
                            targetUser.getPlayer().sendMessage(ConfigurationService.YELLOW + sender.getName() + " has sent you " + ConfigurationService.GOLD + amount + " " + ConfigurationService.YELLOW + (amount == 1 ? "life" : "lives") + ".");
                        }
                    }
                }
                else {
                    UUID target = plugin.getUserManager().fetchUUID(args[1]);
                    FactionUser targetUser;
                    if (target == null || (targetUser = plugin.getUserManager().getUser(target)).getName() == null) {
                        sender.sendMessage(ConfigurationService.RED + "Player not found");
                    } else {
                        if(factionUser != null){
                            factionUser.setLives(factionUser.getLives() - amount);
                        }
                        targetUser.setLives(targetUser.getLives() + amount);
                        sender.sendMessage(ConfigurationService.YELLOW + "You have sent " + ConfigurationService.GOLD + targetUser.getName() + ConfigurationService.YELLOW + " " + amount + " " + ConfigurationService.YELLOW + (amount == 1 ? "life" : "lives") + ".");
                        if (targetUser.isOnline()) {
                            targetUser.getPlayer().sendMessage(ConfigurationService.YELLOW + sender.getName() + " has sent you " + ConfigurationService.GOLD + amount + " " + ConfigurationService.YELLOW + (amount == 1 ? "life" : "lives") + ".");
                        }
                    }
                }
            }
        }
        return true;
    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? null : Collections.emptyList();

    }

}