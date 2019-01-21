package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.listener.PickupListener;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class MobDropsCommand implements CommandExecutor{
    private final HCF hcf;

    public MobDropsCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            FactionUser factionUser = hcf.getUserManager().getUser(player.getUniqueId());
            boolean mobdrops =! factionUser.isNomobdrops();
            factionUser.setNomobdrops(mobdrops);
            if(factionUser.isNomobdrops()){
                player.setMetadata(PickupListener.NO_MOBDROPS_META, new FixedMetadataValue(hcf, true));
            }
            else{
                player.removeMetadata(PickupListener.NO_MOBDROPS_META, hcf);
            }
            sender.sendMessage(ConfigurationService.YELLOW + "You have " + (mobdrops ? ChatColor.RED + "disabled" : ChatColor.GREEN + "enabled") + ConfigurationService.YELLOW + " mob drops");
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "You must be a player to do this");
        }
        return true;
    }
}
