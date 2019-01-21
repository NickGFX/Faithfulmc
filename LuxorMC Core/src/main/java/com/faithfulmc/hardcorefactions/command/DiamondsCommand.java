package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.listener.FoundDiamondsListener;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class DiamondsCommand implements CommandExecutor {
    private final HCF hcf;

    public DiamondsCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            FactionUser factionUser = hcf.getUserManager().getIfContains(player.getUniqueId());
            if(factionUser != null) {
                factionUser.setFdalerts(!factionUser.isFdalerts());
                sender.sendMessage(ConfigurationService.YELLOW + "Diamond alerts are now " + (factionUser.isFdalerts() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                if(factionUser.isFdalerts()){
                    player.removeMetadata(FoundDiamondsListener.NO_DIAMOND_ALERTS, hcf);
                }
                else{
                    player.setMetadata(FoundDiamondsListener.NO_DIAMOND_ALERTS, new FixedMetadataValue(hcf, true));
                }
            }
        } else {
            sender.sendMessage(ConfigurationService.RED + "Only players can do this");
        }
        return true;
    }
}
