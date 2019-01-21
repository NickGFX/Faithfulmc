package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FactionLeaveArgument extends CommandArgument {
    private final HCF plugin;

    public FactionLeaveArgument(HCF plugin) {
        super("leave", "Leave your current faction.");
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage((Object) ConfigurationService.RED + "Only players can leave faction.");
            return true;
        }
        Player player = (Player) sender;
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            sender.sendMessage((Object) ConfigurationService.RED + "You are not in a faction.");
            return true;
        }
        UUID uuid = player.getUniqueId();
        if (playerFaction.getMember(uuid).getRole() == Role.LEADER) {
            sender.sendMessage((Object) ConfigurationService.RED + "You cannot leave factions as a leader. Either use " + (Object) ConfigurationService.GOLD + '/' + label + " disband" + (Object) ConfigurationService.RED + " or " + (Object) ConfigurationService.GOLD + '/' + label + " leader" + (Object) ConfigurationService.RED + '.');
            return true;
        }
        if (playerFaction.setMember(player, null)) {
            sender.sendMessage((Object) ConfigurationService.YELLOW + "Successfully left the faction.");
            playerFaction.broadcast((Object) Relation.ENEMY.toChatColour() + sender.getName() + (Object) ConfigurationService.YELLOW + " has left the faction.");
        }
        return true;
    }
}