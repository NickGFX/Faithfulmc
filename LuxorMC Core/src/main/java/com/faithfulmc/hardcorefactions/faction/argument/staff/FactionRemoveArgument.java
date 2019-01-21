package com.faithfulmc.hardcorefactions.faction.argument.staff;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionRemoveArgument extends CommandArgument {
    private final ConversationFactory factory;
    private final HCF plugin;

    public FactionRemoveArgument(final HCF plugin) {
        super("remove", "Remove a faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"delete", "forcedisband", "forceremove"};
        this.permission = "hcf.command.faction.argument." + this.getName();
        this.factory = new ConversationFactory((Plugin) plugin).withFirstPrompt((Prompt) new RemoveAllPrompt(plugin)).withEscapeSequence("/no").withTimeout(10).withModality(false).withLocalEcho(true);
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <all|factionName>";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        new BukkitRunnable() {
            public void run() {
                if (args.length < 2) {
                    sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
                    return;
                }
                if (args[1].equalsIgnoreCase("all")) {
                    if (!(sender instanceof ConsoleCommandSender)) {
                        sender.sendMessage(ConfigurationService.RED + "This command can be only executed from console.");
                        return;
                    }
                    final Conversable conversable = (Conversable) sender;
                    conversable.beginConversation(factory.buildConversation(conversable));
                    return;
                } else {
                    final Faction faction = plugin.getFactionManager().getContainingFaction(args[1]);
                    if (faction == null) {
                        sender.sendMessage(ConfigurationService.RED + "Faction named or containing member with IGN or UUID " + args[1] + " not found.");
                        return;
                    }
                    if (plugin.getFactionManager().removeFaction(faction, sender)) {
                        Command.broadcastCommandMessage(sender, ConfigurationService.YELLOW + "Disbanded faction " + faction.getName() + ConfigurationService.YELLOW + '.');
                    }
                    return;
                }
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }
        if (args[1].isEmpty()) {
            return null;
        }
        final Player player = (Player) sender;
        final List<String> results = new ArrayList<String>(this.plugin.getFactionManager().getFactionNameMap().keySet());
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (player.canSee(target) && !results.contains(target.getName())) {
                results.add(target.getName());
            }
        }
        return results;
    }

    private static class RemoveAllPrompt extends StringPrompt {
        private final HCF plugin;

        public RemoveAllPrompt(final HCF plugin) {
            this.plugin = plugin;
        }

        public String getPromptText(final ConversationContext context) {
            return ConfigurationService.YELLOW + "Are you sure you want to do this? " + ConfigurationService.RED + ChatColor.BOLD + "All factions" + ConfigurationService.YELLOW + " will be cleared. " + "Type " + ChatColor.GREEN + "yes" + ConfigurationService.YELLOW + " to confirm or " + ConfigurationService.RED + "no" + ConfigurationService.YELLOW + " to deny.";
        }

        public Prompt acceptInput(final ConversationContext context, final String string) {
            final String lowerCase3;
            final String s;
            final String lowerCase2 = s = (lowerCase3 = string.toLowerCase());
            switch (s) {
                case "yes": {
                    for (final Faction faction : this.plugin.getFactionManager().getFactions()) {
                        this.plugin.getFactionManager().removeFaction(faction, (CommandSender) Bukkit.getConsoleSender());
                    }
                    final Conversable conversable = context.getForWhom();
                    Bukkit.broadcastMessage(ConfigurationService.GOLD.toString() + ChatColor.BOLD + "All factions have been disbanded" + ((conversable instanceof CommandSender) ? (" by " + ((CommandSender) conversable).getName()) : "") + '.');
                    return Prompt.END_OF_CONVERSATION;
                }
                case "no": {
                    context.getForWhom().sendRawMessage(ChatColor.BLUE + "Cancelled the process of disbanding all factions.");
                    return Prompt.END_OF_CONVERSATION;
                }
                default: {
                    context.getForWhom().sendRawMessage(ConfigurationService.RED + "Unrecognized response. Process of disbanding all factions cancelled.");
                    return Prompt.END_OF_CONVERSATION;
                }
            }
        }
    }
}