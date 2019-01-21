package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class ServerTimeCommand implements CommandExecutor, org.bukkit.command.TabCompleter {

    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("E MMM dd h:mm:ssa z yyyy", ConfigurationService.SERVER_TIME_ZONE);


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        sender.sendMessage(ConfigurationService.YELLOW + "The server time is " + ConfigurationService.GRAY + FORMAT.format(System.currentTimeMillis()) + ConfigurationService.GRAY + '.');

        return true;

    }


    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        return java.util.Collections.emptyList();

    }

}