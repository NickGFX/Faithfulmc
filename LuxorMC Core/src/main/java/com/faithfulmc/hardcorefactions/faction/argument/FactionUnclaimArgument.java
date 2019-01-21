package com.faithfulmc.hardcorefactions.faction.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionMember;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.chat.ClickAction;
import com.faithfulmc.util.chat.Text;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


public class FactionUnclaimArgument extends CommandArgument {

    private static final HashSet<String> stuff = new HashSet();

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
    private final HCF plugin;


    public FactionUnclaimArgument(HCF plugin) {

        super("unclaim", "Unclaims land from your faction.");

        this.plugin = plugin;

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " ";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "Only players can un-claim land from a faction.");

            return true;

        }

        Player player = (Player) sender;

        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {

            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");

            return true;

        }

        FactionMember factionMember = playerFaction.getMember(player);

        if (factionMember.getRole() != Role.LEADER && factionMember.getRole() != Role.COLEADER) {

            sender.sendMessage(ConfigurationService.RED + "You must be a faction leader to unclaim land.");

            return true;

        }

        Collection<Claim> factionClaims = playerFaction.getClaims();

        if (factionClaims.isEmpty()) {

            sender.sendMessage(ConfigurationService.RED + "Your faction does not own any claims.");

            return true;

        }

        if (args.length == 2) {

            if ((args[1].equalsIgnoreCase("yes")) && (stuff.contains(player.getName()))) {

                for (Claim claims : new ArrayList<>(factionClaims)) {

                    playerFaction.removeClaim(claims, player);

                }

                factionClaims.clear();

                return true;

            }

            if ((args[1].equalsIgnoreCase("no")) && (stuff.contains(player.getName()))) {

                stuff.remove(player.getName());

                player.sendMessage(ConfigurationService.YELLOW + "You have been removed the unclaim-set.");

                return true;

            }

        }

        stuff.add(player.getName());

        new Text(ConfigurationService.YELLOW + "Do you want to unclaim " + ChatColor.BOLD + "all" + ConfigurationService.YELLOW + " of your land?").send(player);

        new Text(ConfigurationService.YELLOW + "If so, " + ChatColor.DARK_GREEN + "/f unclaim yes" + ConfigurationService.YELLOW + " otherwise do" + ChatColor.DARK_RED + " /f unclaim no" + ConfigurationService.GRAY + " (Click here to unclaim)").setHoverText(ConfigurationService.GOLD + "Click here to unclaim all").setClick(ClickAction.RUN_COMMAND, "/f unclaim yes").send(player);

        return true;

    }

}