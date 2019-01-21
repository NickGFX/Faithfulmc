package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.util.BukkitUtils;
import net.minecraft.util.com.google.common.base.Enums;
import net.minecraft.util.com.google.common.base.Optional;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SetBorderCommand implements CommandExecutor, TabCompleter {
    private static final int MIN_SET_SIZE = 50;
    private static final int MAX_SET_SIZE = 25000;


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {

            sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + " <worldType> <amount>");

            return true;

        }

        Optional<World.Environment> optional = Enums.getIfPresent(World.Environment.class, args[0]);

        if (!optional.isPresent()) {

            sender.sendMessage(ConfigurationService.RED + "Environment '" + args[0] + "' not found.");

            return true;

        }

        Integer amount = Ints.tryParse(args[1]);

        if (amount == null) {

            sender.sendMessage(ConfigurationService.RED + "'" + args[1] + "' is not a valid number.");

            return true;

        }

        if (amount < MIN_SET_SIZE) {

            sender.sendMessage(ConfigurationService.RED + "Minimum border size is " + MIN_SET_SIZE + '.');

            return true;

        }

        if (amount > MAX_SET_SIZE) {

            sender.sendMessage(ConfigurationService.RED + "Maximum border size is " + MAX_SET_SIZE + '.');

            return true;

        }

        World.Environment environment = (World.Environment) optional.get();

        ConfigurationService.BORDER_SIZES.put(environment, amount);

        Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Set border size of environment " + environment.name() + " to " + amount + '.');

        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 1) {

            return Collections.emptyList();

        }

        World.Environment[] values = World.Environment.values();

        List<String> results = new ArrayList(values.length);

        for (World.Environment environment : values) {

            results.add(environment.name());

        }

        return BukkitUtils.getCompletions(args, results);

    }

}