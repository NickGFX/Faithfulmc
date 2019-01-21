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

public class CobbleCommand implements CommandExecutor {
    private final HCF hcf;

    public CobbleCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            FactionUser factionUser = hcf.getUserManager().getUser(player.getUniqueId());
            if (factionUser.isNocobble()) {
                player.removeMetadata(PickupListener.NO_COBBLE_META, hcf);
                player.sendMessage(ConfigurationService.YELLOW + "You " + ChatColor.GREEN + "enabled" + ConfigurationService.YELLOW + " cobblestone item pickup");
            } else {
                player.setMetadata(PickupListener.NO_COBBLE_META, new FixedMetadataValue(hcf, true));
                player.sendMessage(ConfigurationService.YELLOW + "You " + ChatColor.RED + "disabled" + ConfigurationService.YELLOW + " cobblestone item pickup");
            }
            factionUser.setNocobble(!factionUser.isNocobble());
        } else {
            sender.sendMessage(ConfigurationService.RED + "You must be a player to do this");
        }
        return true;
    }
}
