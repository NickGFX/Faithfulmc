package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffServerCommand implements CommandExecutor{
    private final HCF hcf;

    public StaffServerCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player){
            if(args.length != 1){
                sender.sendMessage(ConfigurationService.YELLOW + "Invalid Usage: " + ConfigurationService.GRAY + "/" + label + " <server>");
            }
            else {
                ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
                dataOutput.writeUTF("Connect");
                dataOutput.writeUTF(args[0]);
                ((Player) sender).sendPluginMessage(hcf, "BungeeCord", dataOutput.toByteArray());
                sender.sendMessage(ConfigurationService.YELLOW + "Sending you to " + ConfigurationService.GOLD + args[0]);
            }
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "You need to be a player to do this");
        }
        return true;
    }
}
