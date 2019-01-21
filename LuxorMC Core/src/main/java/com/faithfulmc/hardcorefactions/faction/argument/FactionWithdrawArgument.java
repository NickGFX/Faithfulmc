package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionWithdrawArgument extends CommandArgument {
    private static final ImmutableList<String> COMPLETIONS;

    static {
        COMPLETIONS = ImmutableList.of("all");
    }

    private final HCF plugin;

    public FactionWithdrawArgument(final HCF plugin) {
        super("withdraw", "Withdraws money from the faction balance.", new String[]{"w"});
        this.plugin = plugin;
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <all|amount>";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can update the faction balance.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final Player player = (Player) sender;
        final PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            return true;
        }
        final UUID uuid = player.getUniqueId();
        final FactionMember factionMember = playerFaction.getMember(uuid);
        if (factionMember.getRole() == Role.MEMBER) {
            sender.sendMessage(ConfigurationService.RED + "You must be a faction officer to withdraw money.");
            return true;
        }
        final int factionBalance = playerFaction.getBalance();
        Integer amount;
        if (args[1].equalsIgnoreCase("all")) {
            amount = factionBalance;
        } else if ((amount = Ints.tryParse(args[1])) == null) {
            sender.sendMessage(ConfigurationService.RED + "Error: '" + args[1] + "' is not a valid number.");
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(ConfigurationService.RED + "Amount must be positive.");
            return true;
        }
        if (amount > factionBalance) {
            sender.sendMessage(ConfigurationService.RED + "Your faction need at least " + '$' + JavaUtils.format(amount) + " to do this, whilst it only has " + '$' + JavaUtils.format(factionBalance) + '.');
            return true;
        }
        FactionUser factionUser = plugin.getUserManager().getUser(factionMember.getUniqueId());
        factionUser.setBalance(factionUser.getBalance() + amount);
        playerFaction.setBalance(factionBalance - amount);
        playerFaction.broadcast(ConfigurationService.TEAMMATE_COLOUR + factionMember.getRole().getAstrix() + sender.getName() + ConfigurationService.YELLOW + " has withdrew " + ChatColor.BOLD + '$' + JavaUtils.format(amount) + ConfigurationService.YELLOW + " from the faction balance.");
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 2) ? FactionWithdrawArgument.COMPLETIONS : Collections.emptyList();
    }
}