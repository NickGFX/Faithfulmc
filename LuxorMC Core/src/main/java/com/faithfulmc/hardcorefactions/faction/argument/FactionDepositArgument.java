package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.collect.ImmutableList;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionDepositArgument extends CommandArgument {
    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
    private final HCF plugin;

    public FactionDepositArgument(HCF plugin) {
        super("deposit", "Deposits money to the faction balance.", new String[]{"d"});
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <all|amount>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
            return true;
        }
        Player player = (Player) sender;
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            return true;
        }
        UUID uuid = player.getUniqueId();
        FactionUser factionUser = this.plugin.getUserManager().getUser(uuid);
        int playerBalance = factionUser.getBalance();
        Integer amount;
        if (args[1].equalsIgnoreCase("all")) {
            amount = playerBalance;
        } else if ((amount = Ints.tryParse(args[1])) == null) {
            sender.sendMessage(ConfigurationService.RED + "'" + args[1] + "' is not a valid number.");
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(ConfigurationService.RED + "Amount must be positive.");
            return true;
        }
        if (playerBalance < amount) {
            sender.sendMessage(ConfigurationService.RED + "You need at least " + '$' + JavaUtils.format(amount) + " to do this, you only have " + '$' + JavaUtils.format(Integer.valueOf(playerBalance)) + '.');
            return true;
        }
        factionUser.setBalance(factionUser.getBalance() - amount);
        playerFaction.setBalance(playerFaction.getBalance() + amount);
        playerFaction.broadcast(Relation.MEMBER.toChatColour() + playerFaction.getMember(player).getRole().getAstrix() + sender.getName() + ConfigurationService.YELLOW + " has deposited " + ChatColor.GREEN + '$' + JavaUtils.format(amount) + ConfigurationService.YELLOW + " into the faction balance.");
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? COMPLETIONS : Collections.emptyList();
    }
}
