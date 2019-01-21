package com.faithfulmc.hardcorefactions.faction.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;


public class FactionRenameArgument extends CommandArgument {

    private static final long FACTION_RENAME_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(15L);

    private static final String FACTION_RENAME_DELAY_WORDS = DurationFormatUtils.formatDurationWords(FACTION_RENAME_DELAY_MILLIS, true, true);
    private final HCF plugin;


    public FactionRenameArgument(HCF plugin) {

        super("rename", "Change the name of your faction.");

        this.plugin = plugin;

        this.aliases = new String[]{"changename", "setname"};

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <newFactionName>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "Only players can create faction.");

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

        if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER && playerFaction.getMember(player.getUniqueId()).getRole() != Role.COLEADER) {

            sender.sendMessage(ConfigurationService.RED + "You must be a faction leader to edit the name.");

            return true;

        }

        String newName = args[1];

        if (ConfigurationService.DISALLOWED_FACTION_NAMES.contains(newName.toLowerCase())) {
                    sender.sendMessage(ConfigurationService.RED + "'" + newName + "' is a blocked faction name.");

            return true;

        }

        if (newName.length() < 3) {

            sender.sendMessage(ConfigurationService.RED + "Faction names must have at least " + 3 + " characters.");

            return true;

        }

        if (newName.length() > 16) {

            sender.sendMessage(ConfigurationService.RED + "Faction names cannot be longer than " + 16 + " characters.");

            return true;

        }

        if (!JavaUtils.isAlphanumeric(newName)) {

            sender.sendMessage(ConfigurationService.RED + "Faction names may only be alphanumeric.");

            return true;

        }

        if (this.plugin.getFactionManager().getFaction(newName) != null) {

            sender.sendMessage(ConfigurationService.RED + "Faction " + newName + ConfigurationService.RED + " already exists.");

            return true;

        }

        long difference = playerFaction.lastRenameMillis - System.currentTimeMillis() + FACTION_RENAME_DELAY_MILLIS;

        if ((!player.isOp()) && (difference > 0L)) {

            player.sendMessage(ConfigurationService.RED + "There is a faction rename delay of " + FACTION_RENAME_DELAY_WORDS + ". Therefore you need to wait another " + DurationFormatUtils.formatDurationWords(difference, true, true) + " to rename your faction.");

            return true;

        }

        playerFaction.setName(args[1], sender);

        return true;

    }

}