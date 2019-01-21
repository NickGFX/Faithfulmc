package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetMaxPlayersCommand extends BaseCommand {
    public SetMaxPlayersCommand() {
        super("setmaxplayers", "Sets the max player cap.");
        this.setAliases(new String[]{"setplayercap"});
        this.setUsage("/(command) <amount>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Integer amount = Ints.tryParse(args[0]);
        if (amount == null) {
            sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a number.");
            return true;
        }
        Bukkit.setMaxPlayers((int) amount);
        Command.broadcastCommandMessage(sender, BaseConstants.YELLOW + "Set the maximum players to " + amount + '.');
        return true;
    }
}
