package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.listener.OreListener;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class OreInventoryCommand implements CommandExecutor {
    private final HCF hcf;

    public OreInventoryCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            FactionUser factionUser = hcf.getUserManager().getIfContains(player.getUniqueId());
            if(factionUser != null) {
                factionUser.setOreInventory(!factionUser.isOreInventory());
                sender.sendMessage(ConfigurationService.YELLOW + "Automatic ore pickup is now " + (factionUser.isOreInventory() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                if(factionUser.isOreInventory()){
                    player.removeMetadata(OreListener.NO_OREINVENTORY_META, hcf);
                }
                else{
                    player.setMetadata(OreListener.NO_OREINVENTORY_META, new FixedMetadataValue(hcf, true));
                }
            }
        } else {
            sender.sendMessage(ConfigurationService.RED + "Only players can do this");
        }
        return true;
    }
}
