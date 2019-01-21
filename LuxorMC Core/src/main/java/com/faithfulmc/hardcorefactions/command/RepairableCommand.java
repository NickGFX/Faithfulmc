package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RepairableCommand implements CommandExecutor{
    public static final String REPAIRABLE = ConfigurationService.RED + "Repairable";
    private final HCF plugin;

    public RepairableCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            ItemStack itemStack = player.getItemInHand();
            if(itemStack != null){
                ItemMeta itemMeta = itemStack.getItemMeta();
                if(itemMeta != null){
                    List<String> currentLore = itemMeta.getLore();
                    if(currentLore == null){
                        currentLore = new ArrayList<>();
                    }
                    if(currentLore.remove(REPAIRABLE)){
                        sender.sendMessage(ConfigurationService.YELLOW + "Your current item is no longer repairable");
                    }
                    else{
                        currentLore.add(REPAIRABLE);
                        sender.sendMessage(ConfigurationService.YELLOW + "Your current item is now repairable");
                    }
                    itemMeta.setLore(currentLore);
                    itemStack.setItemMeta(itemMeta);
                    player.setItemInHand(itemStack);
                }
                else{
                    sender.sendMessage(ConfigurationService.RED + "You must have an item in your hand");
                }
            }
            else{
                sender.sendMessage(ConfigurationService.RED + "You must have an item in your hand");
            }
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "You need to be a player to do this");
        }
        return true;
    }
}
