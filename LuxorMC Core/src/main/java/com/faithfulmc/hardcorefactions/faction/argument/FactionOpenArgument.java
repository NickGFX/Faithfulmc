package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionOpenArgument extends CommandArgument {
    private final HCF plugin;

    public FactionOpenArgument(HCF plugin) {
        super("open", "Opens the faction to the public.");
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + this.getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        Player player = (Player) sender;
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            return true;
        }
        FactionMember factionMember = playerFaction.getMember(player.getUniqueId());
        if (factionMember.getRole() != Role.LEADER && factionMember.getRole() != Role.COLEADER) {
            sender.sendMessage(ConfigurationService.RED + "You must be a faction leader to do this.");
            return true;
        }
        boolean newOpen = !playerFaction.isOpen();
        playerFaction.setOpen(newOpen);
        playerFaction.broadcast(ConfigurationService.YELLOW + sender.getName() + " has " + (newOpen ? (ChatColor.GREEN + "opened") : (ConfigurationService.RED + "closed")) + ConfigurationService.YELLOW + " the faction to public.");
        return true;
    }
}