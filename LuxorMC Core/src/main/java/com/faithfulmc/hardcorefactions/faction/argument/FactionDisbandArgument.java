package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionDisbandArgument extends CommandArgument {
    private final HCF plugin;

    public FactionDisbandArgument(HCF plugin) {
        super("disband", "Disband your faction.");
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage((Object) ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        Player player = (Player) sender;
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            sender.sendMessage((Object) ConfigurationService.RED + "You are not in a faction.");
            return true;
        }
        if (!ConfigurationService.KIT_MAP) {
            if (playerFaction.isRaidable() && !this.plugin.getEotwHandler().isEndOfTheWorld()) {
                sender.sendMessage((Object) ConfigurationService.RED + "You cannot disband your faction while it is raidable.");
                return true;
            }
        }
        if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
            sender.sendMessage((Object) ConfigurationService.RED + "You must be a leader to disband the faction.");
            return true;
        }
        this.plugin.getFactionManager().removeFaction(playerFaction, sender);
        return true;
    }
}