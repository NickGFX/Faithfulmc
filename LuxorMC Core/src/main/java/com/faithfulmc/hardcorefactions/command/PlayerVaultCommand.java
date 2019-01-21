package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PlayerVaultCommand implements CommandExecutor{
    private final HCF hcf;

    public PlayerVaultCommand(HCF hcf) {
        this.hcf = hcf;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            int rows = hcf.getVaultManager().getRows(sender);
            if (rows <= 0) {
                sender.sendMessage(ConfigurationService.RED + "You do not have permission to use a vault");
            } else {
                Player player = (Player) sender;
                if(hcf.getFactionManager().getFactionAt(player.getLocation()).isSafezone()){
                    Inventory inventory = hcf.getVaultManager().createVault(player, rows);
                    player.openInventory(inventory);
                    player.sendMessage(ConfigurationService.YELLOW + "Opening your vault");
                }
                else{
                    player.sendMessage(ConfigurationService.YELLOW + "You need to be in " + ChatColor.AQUA + "Spawn" + ConfigurationService.YELLOW + " to do this.");
                }
            }
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "You need to be a player to do this");
        }
        return true;
    }
}
