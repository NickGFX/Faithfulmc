package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.scoreboard.PlayerBoard;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;


public class FocusCommand implements CommandExecutor {
    private final HCF hcf;

    public FocusCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        UUID target;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "You must be a player to do this");
        } else if (args.length == 0) {
            Player player = (Player) sender;
            PlayerFaction playerFaction = hcf.getFactionManager().getPlayerFaction(player);
            String focusname = playerFaction != null ? playerFaction.getFocusname() : null;
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Focused Player: " + ConfigurationService.GRAY + (focusname == null ? "None" : focusname));
            if (focusname == null) {
                player.sendMessage(ConfigurationService.RED + "To set a focus miner /" + label + " help");
            }
        } else if ((target = hcf.getUserManager().fetchUUID(args[0])) != null) {
            Player player = (Player) sender;
            Player targetPlayer = Bukkit.getPlayer(target);
            if (player.getUniqueId() == target) {
                player.sendMessage(ConfigurationService.RED + "You may not focus yourself");
            } else {
                PlayerFaction playerFaction = hcf.getFactionManager().getPlayerFaction(player);
                PlayerFaction targetFaction = hcf.getFactionManager().getPlayerFaction(target);
                if (playerFaction == null) {
                    sender.sendMessage(ConfigurationService.RED + "You must be in a faction to focus a player");
                } else if (playerFaction == targetFaction) {
                    sender.sendMessage(ConfigurationService.RED + "That player is a member of your faction");
                } else if (playerFaction.getFocus() == target) {
                    playerFaction.setFocus(null);
                    playerFaction.setFocusname(null);
                    playerFaction.broadcast(ChatColor.LIGHT_PURPLE + "Your faction's focus has been reset by " + ConfigurationService.GRAY + sender.getName());
                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        for (Player other : playerFaction.getOnlinePlayers()) {
                            hcf.getScoreboardHandler().getPlayerBoard(other.getUniqueId()).init(targetPlayer);
                        }
                    }
                } else {
                    FactionUser factionUser = hcf.getUserManager().getUser(target);
                    if(factionUser != null && factionUser.getName() != null) {
                        UUID oldFocus = playerFaction.getFocus();
                        Player oldFocusPlayer = oldFocus == null ? null : Bukkit.getPlayer(oldFocus);
                        playerFaction.setFocusname(factionUser.getName());
                        playerFaction.setFocus(target);
                        playerFaction.broadcast(ChatColor.LIGHT_PURPLE + "Your faction's focus has been set to " + ConfigurationService.GRAY + factionUser.getName() + ChatColor.LIGHT_PURPLE + " by " + ConfigurationService.GRAY + sender.getName());
                        for (Player other : playerFaction.getOnlinePlayers()) {
                            PlayerBoard playerBoard = hcf.getScoreboardHandler().getPlayerBoard(other.getUniqueId());
                            if(targetPlayer != null && targetPlayer.isOnline()) {
                                playerBoard.init(targetPlayer);
                            }
                            if(oldFocusPlayer != null){
                                playerBoard.init(oldFocusPlayer);
                            }
                        }
                    }
                    else{
                        sender.sendMessage(ConfigurationService.RED + "Player not found, use /" + label + " help for more information");
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/" + label + ConfigurationService.GRAY + " (Views current focus)");
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "/" + label + " <player>" + ConfigurationService.GRAY + " (Focuses/Unfocuses a player)");
        } else {
            sender.sendMessage(ConfigurationService.RED + "Player not found, use /" + label + " help for more information");
        }
        return true;
    }
}

