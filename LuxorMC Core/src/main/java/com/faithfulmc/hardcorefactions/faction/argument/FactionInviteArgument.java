package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.chat.ClickAction;
import com.faithfulmc.util.chat.Text;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Pattern;


public class FactionInviteArgument extends CommandArgument {
    private static final Pattern USERNAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");
    private final HCF plugin;

    public FactionInviteArgument(HCF plugin) {
        super("invite", "Invite a player to the faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"inv", "invitemember", "inviteplayer"};
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can invite to a faction.");
        } else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        } else if (!USERNAME_REGEX.matcher(args[1]).matches()) {
            sender.sendMessage(ConfigurationService.RED + "'" + args[1] + "' is an invalid username.");
        } else {
            Player player = (Player) sender;
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if (playerFaction == null) {
                sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");;
            } else if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
                sender.sendMessage(ConfigurationService.RED + "You must a faction officer to invite members.");
            } else {
                Set<String> invitedPlayerNames = playerFaction.getInvitedPlayerNames();
                String name = args[1];
                UUID uuid = plugin.getUserManager().fetchUUID(name);
                if(uuid != null){
                    FactionUser factionUser = plugin.getUserManager().getUser(uuid);
                    if(factionUser != null){
                        name = factionUser.getName();
                    }
                }
                if (playerFaction.getMember(plugin, name) != null) {
                    sender.sendMessage(ConfigurationService.RED + "'" + name + "' is already in your faction.");
                } else if ((!plugin.getEotwHandler().isEndOfTheWorld()) && (playerFaction.isRaidable())) {
                    sender.sendMessage(ConfigurationService.RED + "You may not invite players whilst your faction is raidable.");
                } else if (!invitedPlayerNames.add(name)) {
                    sender.sendMessage(ConfigurationService.RED + name + " has already been invited.");
                } else {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target != null) {
                        name = target.getName();
                        Text text = new Text(sender.getName()).setColor(Relation.ENEMY.toChatColour()).append(new Text(" has invited you to join ").setColor(ConfigurationService.YELLOW));
                        text.append(new Text(playerFaction.getName()).setColor(Relation.ENEMY.toChatColour())).append(new Text(". ").setColor(ConfigurationService.YELLOW));
                        text.append(new Text("Click here").setColor(ChatColor.GREEN).setClick(ClickAction.RUN_COMMAND, '/' + label + " accept " + playerFaction.getName()).setHoverText(ConfigurationService.GRAY + "Click to join " + playerFaction.getDisplayName(target) + ConfigurationService.GRAY + '.')).append(new Text(" to accept this invitation.").setColor(ConfigurationService.YELLOW));
                        text.send(target);
                    }
                    playerFaction.broadcast(Relation.MEMBER.toChatColour() + sender.getName() + ConfigurationService.YELLOW + " has invited " + Relation.ENEMY.toChatColour() + name + ConfigurationService.YELLOW + " to the faction.");
                }
            }
        }
        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if ((args.length != 2) || (!(sender instanceof Player))) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if ((playerFaction == null) || (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER)) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if ((player.canSee(target)) && (!results.contains(target.getName()))) {
                Faction targetFaction = this.plugin.getFactionManager().getPlayerFaction(target.getUniqueId());
                if ((targetFaction == null) || (!targetFaction.equals(playerFaction))) {
                    results.add(target.getName());
                }
            }
        }
        return results;

    }

}