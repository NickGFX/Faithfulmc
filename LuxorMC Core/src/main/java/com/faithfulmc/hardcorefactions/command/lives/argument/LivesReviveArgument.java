package com.faithfulmc.hardcorefactions.command.lives.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.deathban.Deathban;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class LivesReviveArgument extends CommandArgument {
    private static final String REVIVE_BYPASS_PERMISSION = "hcf.revive.bypass";
    private static final String PROXY_CHANNEL_NAME = "BungeeCord";
    private final HCF plugin;

    public LivesReviveArgument(HCF plugin) {
        super("revive", "Revive a death-banned player");
        this.plugin = plugin;
        this.permission = ("hcf.command.lives.argument." + getName());
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, PROXY_CHANNEL_NAME);
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            if(!sender.hasPermission(REVIVE_BYPASS_PERMISSION) && plugin.getEotwHandler().isEndOfTheWorld()){
                sender.sendMessage(ConfigurationService.RED + "You cannot revive players during EOTW.");
            }
            else {
                Player senderPlayer = sender instanceof Player ? (Player) sender : null;
                FactionUser factionUser = senderPlayer == null ? null : plugin.getUserManager().getUser(senderPlayer.getUniqueId());
                UUID target = plugin.getUserManager().fetchUUID(args[1]);
                FactionUser targetUser;
                Deathban deathban;
                if (target == null || (targetUser = plugin.getUserManager().getUser(target)).getName() == null) {
                    sender.sendMessage(ConfigurationService.RED + "Player not found");
                } else if ((deathban = targetUser.getDeathban()) == null || !deathban.isActive()) {
                    sender.sendMessage(ConfigurationService.RED + "Player is not death-banned");
                } else {
                    Relation relation = factionUser == null ? Relation.ENEMY : factionUser.getRelation(targetUser);
                    if (factionUser == null || sender.hasPermission(REVIVE_BYPASS_PERMISSION)) {
                        sender.sendMessage(ConfigurationService.YELLOW + "You have revived " + relation.toChatColour() + targetUser.getName() + ConfigurationService.YELLOW + '.');
                        targetUser.removeDeathban();
                    } else if (factionUser.getLives() <= 0) {
                        sender.sendMessage(ConfigurationService.RED + "You do not have any lives");
                    } else {
                        factionUser.setLives(factionUser.getLives() - 1);
                        targetUser.removeDeathban();
                        sender.sendMessage(ConfigurationService.YELLOW + "You have revived " + relation.toChatColour() + targetUser.getName() + ConfigurationService.YELLOW + '.');
                    }
                }
            }
        }
        return true;
    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();

    }

}