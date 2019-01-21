package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.DescParseTickFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;

public class PTimeCommand implements CommandExecutor{
    private final HCF hcf;

    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

    public PTimeCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player)sender;
            long offset = player.getPlayerTimeOffset();
            long time = player.getPlayerTime();
            if (args.length == 0) {
                if(offset != 0) {
                    sender.sendMessage(ConfigurationService.YELLOW + "Your current time is " + ConfigurationService.GRAY + DescParseTickFormat.formatDateFormat(time, format));
                    sender.sendMessage(ConfigurationService.YELLOW + "To reset your offset type " + ConfigurationService.GRAY + "/" + label + " reset");
                }
                else{
                    sender.sendMessage(ConfigurationService.YELLOW + "You currently have no time offset");
                    sender.sendMessage(ConfigurationService.YELLOW + "To set your time use the command " + ConfigurationService.GRAY + "/" + label + " <time>");
                }
            }
            else if(args[0].equalsIgnoreCase("reset")){
                if(offset == 0){
                    sender.sendMessage(ConfigurationService.YELLOW + "You currently have no time offset");
                }
                else{
                    sender.sendMessage(ConfigurationService.YELLOW + "Your time offset has been reset");
                    sender.sendMessage(ConfigurationService.YELLOW + "Your current time is " + ConfigurationService.GRAY + DescParseTickFormat.formatDateFormat(time, format));
                    player.resetPlayerTime();
                }
            }
            else{
                long ticks;
                try {
                    ticks = DescParseTickFormat.parse(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ConfigurationService.YELLOW + "Invalid time");
                    return true;
                }
                player.resetPlayerTime();
                player.setPlayerTime(ticks, false);
                sender.sendMessage(ConfigurationService.YELLOW + "Time set to " + ConfigurationService.GRAY + DescParseTickFormat.formatDateFormat(ticks, format));
            }
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "You must be a player to do this");
        }
        return true;
    }
}
