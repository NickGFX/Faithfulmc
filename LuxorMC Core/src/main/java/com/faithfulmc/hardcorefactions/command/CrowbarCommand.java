package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.util.Crowbar;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.base.Optional;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CrowbarCommand implements CommandExecutor, TabCompleter {
    private final List<String> completions;

    public CrowbarCommand() {
        this.completions = Arrays.asList("spawn", "setspawners", "setendframes");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + " <spawn|setspawners|setendframes>");
            return true;
        }
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("spawn")) {
            ItemStack stack = new Crowbar().getItemIfPresent();
            player.getInventory().addItem(stack);
            sender.sendMessage(ConfigurationService.YELLOW + "You have given yourself a " + stack.getItemMeta().getDisplayName() + ConfigurationService.YELLOW + '.');
            return true;
        }
        Optional<Crowbar> crowbarOptional = Crowbar.fromStack(player.getItemInHand());

        if (!crowbarOptional.isPresent()) {

            sender.sendMessage(ConfigurationService.RED + "You are not holding a Crowbar.");

            return true;

        }

        if (args[0].equalsIgnoreCase("setspawners")) {

            if (args.length < 2) {

                sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + ' ' + args[0].toLowerCase() + " <amount>");

                return true;

            }

            Integer amount = Ints.tryParse(args[1]);

            if (amount == null) {

                sender.sendMessage(ConfigurationService.RED + "'" + args[1] + "' is not a number.");

                return true;

            }

            if (amount.intValue() < 0) {

                sender.sendMessage(ConfigurationService.RED + "You cannot set Spawner uses to an amount less than " + 0 + '.');

                return true;

            }

            if (amount.intValue() > 1) {

                sender.sendMessage(ConfigurationService.RED + "Crowbars have maximum Spawner uses of " + 1 + '.');

                return true;

            }

            Crowbar crowbar = (Crowbar) crowbarOptional.get();

            crowbar.setSpawnerUses(amount.intValue());

            player.setItemInHand(crowbar.getItemIfPresent());

            sender.sendMessage(ConfigurationService.YELLOW + "Set Spawner uses of held Crowbar to " + amount + '.');

            return true;

        }

        if (!args[0].equalsIgnoreCase("setendframes")) {

            sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + " <spawn|setspawners|setendframes>");

            return true;

        }

        if (args.length < 2) {

            sender.sendMessage(ConfigurationService.RED + "Usage:  / " + label + ' ' + args[0].toLowerCase() + " < amount > ");
            return true;

        }

        Integer amount = Ints.tryParse(args[1]);

        if (amount == null) {

            sender.sendMessage(ConfigurationService.RED + "'" + args[1] + "' is not a number.");

            return true;

        }

        if (amount.intValue() < 0) {

            sender.sendMessage(ConfigurationService.RED + "You cannot set End Frame uses to an amount less than " + 0 + '.');

            return true;

        }

        if (amount.intValue() > 5) {

            sender.sendMessage(ConfigurationService.RED + "Crowbars have maximum End Frame uses of " + 1 + '.');

            return true;

        }

        Crowbar crowbar = (Crowbar) crowbarOptional.get();

        crowbar.setEndFrameUses(amount.intValue());

        player.setItemInHand(crowbar.getItemIfPresent());

        sender.sendMessage(ConfigurationService.YELLOW + "Set End Frame uses of held Crowbar to " + amount + '.');

        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        return args.length == 1 ? BukkitUtils.getCompletions(args, this.completions) : Collections.emptyList();

    }

}