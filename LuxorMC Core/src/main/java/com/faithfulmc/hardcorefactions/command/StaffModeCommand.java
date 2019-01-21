package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffModeCommand implements CommandExecutor {
    private final HCF hcf;

    public StaffModeCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("staffmode.use")) {
                hcf.getStaffModeListener().toggleStaff(player);
            } else {
                player.sendMessage(ConfigurationService.RED + "You do not have permission for this command");
            }
        } else {
            sender.sendMessage(ConfigurationService.RED + "You need to be a player to do this");
        }
        return true;
    }
}
