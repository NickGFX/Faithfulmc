package com.faithfulmc.hardcorefactions.command.lives.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class LivesClearDeathbansArgument extends CommandArgument {
    private final HCF plugin;


    public LivesClearDeathbansArgument(HCF plugin) {

        super("cleardeathbans", "Clears the global deathbans");

        this.plugin = plugin;

        this.aliases = new String[]{"resetdeathbans"};

        this.permission = ("hcf.command.lives.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName();

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (((sender instanceof org.bukkit.command.ConsoleCommandSender)) || (((sender instanceof org.bukkit.entity.Player)) && (sender.getName().equalsIgnoreCase("CommandoNanny")))) {

            for (FactionUser user : this.plugin.getUserManager().getUsers().values()) {

                user.removeDeathban();

            }

            Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "All death-bans have been cleared.");

            return true;

        }

        sender.sendMessage(ConfigurationService.RED + "Must be console");

        return false;

    }

}