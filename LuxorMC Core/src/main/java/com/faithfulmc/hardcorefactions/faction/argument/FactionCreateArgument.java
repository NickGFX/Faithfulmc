package com.faithfulmc.hardcorefactions.faction.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.util.Cooldowns;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class FactionCreateArgument extends CommandArgument {
    private final HCF plugin;

    public static final int FACTION_COOLDON = 120;


    public FactionCreateArgument(HCF plugin) {

        super("create", "Create a faction.", new String[]{"make", "define"});

        this.plugin = plugin;

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <factionName>";

    }


    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "This command may only be executed by players.");

            return true;

        }

        if (args.length < 2) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }

        Player p = (Player) sender;

        String name = args[1];

        if (ConfigurationService.DISALLOWED_FACTION_NAMES.contains(name.toLowerCase())) {

            sender.sendMessage(ConfigurationService.RED + "'" + name + "' is a blocked faction name.");

            return true;

        }
                if (name.length() < 3) {

            sender.sendMessage(ConfigurationService.RED + "Faction names must have at least " + 3 + " characters.");

            return true;

        }

        if (name.length() > 16) {

            sender.sendMessage(ConfigurationService.RED + "Faction names cannot be longer than " + 16 + " characters.");

            return true;

        }

        if (!JavaUtils.isAlphanumeric(name)) {

            sender.sendMessage(ConfigurationService.RED + "Faction names may only be alphanumeric.");

            return true;

        }

        if (this.plugin.getFactionManager().getFaction(name) != null) {

            sender.sendMessage(ConfigurationService.RED + "Faction '" + name + "' already exists.");

            return true;

        }

        if (this.plugin.getFactionManager().getPlayerFaction((Player) sender) != null) {

            sender.sendMessage(ConfigurationService.RED + "You are already in a faction.");

            return true;

        }

        if (Cooldowns.isOnCooldown("Faction_cooldown", p)) {

            p.sendMessage(ConfigurationService.RED + "You cannot create a faction for another: " + ConfigurationService.YELLOW + org.apache.commons.lang.time.DurationFormatUtils.formatDurationWords(Cooldowns.getCooldownForPlayerLong("Faction_cooldown", p), true, true) + ConfigurationService.RED + ".");

            return true;

        }

        Cooldowns.addCooldown("Faction_cooldown", p, FACTION_COOLDON);

        this.plugin.getFactionManager().createFaction(new PlayerFaction(name), sender);

        return true;

    }

}