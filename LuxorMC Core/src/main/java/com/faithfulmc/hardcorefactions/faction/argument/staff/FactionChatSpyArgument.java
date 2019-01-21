package com.faithfulmc.hardcorefactions.faction.argument.staff;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.event.FactionChatEvent;
import com.faithfulmc.hardcorefactions.faction.event.FactionRemoveEvent;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.collect.ImmutableList;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class FactionChatSpyArgument extends CommandArgument implements Listener {
    private static final UUID ALL_UUID = UUID.fromString("5a3ed6d1-0239-4e24-b4a9-8cd5b3e5fc72");
    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("list", "add", "del", "clear");
    private final HCF plugin;

    public FactionChatSpyArgument(HCF plugin) {
        super("chatspy", "Spy on the chat of a faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"cs"};
        this.permission = ("hcf.command.faction.argument." + getName());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <" + StringUtils.join(COMPLETIONS, '|') + "> [factionName]";
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(FactionRemoveEvent event) {
        UUID factionUUID;
        if ((event.getFaction() instanceof PlayerFaction)) {
            factionUUID = event.getFaction().getUniqueID();
            for (FactionUser user : this.plugin.getUserManager().getUsers().values()) {
                user.getFactionChatSpying().remove(factionUUID);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionChat(FactionChatEvent event) {
        Player player = event.getPlayer();
        Faction faction = event.getFaction();
        String format = ConfigurationService.GOLD + "[" + ConfigurationService.RED + event.getChatChannel().getDisplayName() + ": " + ConfigurationService.YELLOW + faction.getName() + ConfigurationService.GOLD + "] " + ConfigurationService.GRAY + event.getFactionMember().getRole().getAstrix() + player.getName() + ": " + ConfigurationService.YELLOW + event.getMessage();
        Set<CommandSender> recipients = new HashSet<>();
        recipients.removeAll(event.getRecipients());
        for (CommandSender recipient : recipients) {
            if ((recipient instanceof Player)) {
                Player target = (Player) recipient;
                FactionUser user = event.isAsynchronous() ? this.plugin.getUserManager().getUser(target.getUniqueId()) : this.plugin.getUserManager().getUser(player.getUniqueId());
                Collection<UUID> spying = user.getFactionChatSpying();
                if ((spying.contains(ALL_UUID)) || (spying.contains(faction.getUniqueID()))) {
                    recipient.sendMessage(format);
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
        }
        else if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
        }
        else {
            Player player = (Player) sender;
            Set<UUID> currentSpies = plugin.getUserManager().getUser(player.getUniqueId()).getFactionChatSpying();
            if (args[1].equalsIgnoreCase("list")) {
                if (currentSpies.isEmpty()) {
                    sender.sendMessage(ConfigurationService.RED + "You are not spying on the chat of any factions.");
                } else {
                    sender.sendMessage(ConfigurationService.GRAY + "You are currently spying on the chat of (" + currentSpies.size() + " factions): " + ConfigurationService.RED + StringUtils.join(currentSpies, new StringBuilder().append(ConfigurationService.GRAY).append(", ").append(ConfigurationService.RED).toString()) + ConfigurationService.GRAY + '.');
                }
            } else if (args[1].equalsIgnoreCase("add")) {
                if (args.length < 3) {
                    sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + ' ' + args[1].toLowerCase() + " <all|factionName|playerName>");
                } else {
                    Faction faction = plugin.getFactionManager().getFaction(args[2]);
                    if (!(faction instanceof PlayerFaction)) {
                        sender.sendMessage(ConfigurationService.RED + "Player based faction named or containing member with IGN or UUID " + args[2] + " not found.");
                    } else if ((currentSpies.contains(ALL_UUID)) || (currentSpies.contains(faction.getUniqueID()))) {
                        sender.sendMessage(ConfigurationService.RED + "You are already spying on the chat of " + (args[2].equalsIgnoreCase("all") ? "all factions" : args[2]) + '.');
                    } else if (args[2].equalsIgnoreCase("all")) {
                        currentSpies.clear();
                        currentSpies.add(ALL_UUID);
                        sender.sendMessage(ChatColor.GREEN + "You are now spying on the chat of all factions.");
                    } else if (currentSpies.add(faction.getUniqueID())) {
                        sender.sendMessage(ChatColor.GREEN + "You are now spying on the chat of " + faction.getDisplayName(sender) + ChatColor.GREEN + '.');
                    } else {
                        sender.sendMessage(ConfigurationService.RED + "You are already spying on the chat of " + faction.getDisplayName(sender) + ConfigurationService.RED + '.');
                    }
                }
            } else if ((args[1].equalsIgnoreCase("del")) || (args[1].equalsIgnoreCase("delete")) || (args[1].equalsIgnoreCase("remove"))) {
                if (args.length < 3) {
                    sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + ' ' + args[1].toLowerCase() + " <playerName>");
                } else if (args[2].equalsIgnoreCase("all")) {
                    currentSpies.remove(ALL_UUID);
                    sender.sendMessage(ConfigurationService.RED + "No longer spying on the chat of all factions.");
                } else {
                    Faction faction = plugin.getFactionManager().getContainingFaction(args[2]);
                    if (faction == null) {
                        sender.sendMessage(ConfigurationService.GOLD + "Faction '" + ConfigurationService.WHITE + args[2] + ConfigurationService.GOLD + "' not found.");
                    } else if (currentSpies.remove(faction.getUniqueID())) {
                        sender.sendMessage(ConfigurationService.RED + "You are no longer spying on the chat of " + faction.getDisplayName(sender) + ConfigurationService.RED + '.');
                    } else {
                        sender.sendMessage(ConfigurationService.RED + "You will still not be spying on the chat of " + faction.getDisplayName(sender) + ConfigurationService.RED + '.');
                    }
                }
            } else if (args[1].equalsIgnoreCase("clear")) {
                currentSpies.clear();
                sender.sendMessage(ConfigurationService.YELLOW + "You are no longer spying the chat of any faction.");
            } else {
                sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
            }
        }
        return true;

    }

}