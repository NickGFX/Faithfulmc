package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class OreAmountCommand implements CommandExecutor {
    public static final ImmutableMap<Material, ChatColor> ORES = new ImmutableMap.Builder<Material, ChatColor>().put(Material.EMERALD_ORE, ChatColor.GREEN).put(Material.DIAMOND_ORE, ChatColor.AQUA).put(Material.REDSTONE_ORE, ChatColor.RED).put(Material.GOLD_ORE, ChatColor.GOLD).put(Material.IRON_ORE, ChatColor.GRAY).put(Material.COAL_ORE, ChatColor.DARK_GRAY).put(Material.LAPIS_ORE, ChatColor.BLUE).build();

    public static String capitalizeString(String string) {
        string = string.replace("_", " ");
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
                found = false;
            }
        } return String.valueOf(chars);
    }

    private final HCF plugin;

    public OreAmountCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (sender instanceof Player && args.length < 1) {
            if (args.length < 1) {
                Player player = (Player) sender;
                FactionUser factionUser = plugin.getUserManager().getIfContains(player.getUniqueId());
                if(factionUser != null){
                    printDetails(sender, factionUser, true);
                }
            }
        } else if (args.length > 0) {
            String playerName = args[0];
            UUID playerUID = plugin.getUserManager().fetchUUID(playerName);
            FactionUser factionUser;
            if (playerUID == null || (factionUser = plugin.getUserManager().getUser(playerUID)).getName() == null) {
                sender.sendMessage(ConfigurationService.RED + "Player not found");
            } else {
                printDetails(sender, factionUser, false);
            }
        } else {
            sender.sendMessage(ConfigurationService.RED + "You must be a player to do this");
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            List<String> results = new ArrayList<>();
            Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (senderPlayer == null || senderPlayer.canSee(target)) {
                    results.add(target.getName());
                }
            }
            return BukkitUtils.getCompletions(args, results);
        }
        return Collections.emptyList();
    }

    public void printDetails(CommandSender sender, FactionUser factionUser, boolean self){
        sender.sendMessage(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + (self ? "Your" : factionUser.getName() + "\'s") + " Ores:");
        Map<Integer, Integer> ores = factionUser.getOres();
        for (Map.Entry<Material, ChatColor> entry : ORES.entrySet()) {
            Material material = entry.getKey();
            ChatColor color = entry.getValue();
            String name = capitalizeString(material.name());
            int amt = ores.getOrDefault(material.getId(), 0);
            if (material == Material.REDSTONE) {
                amt += ores.getOrDefault(Material.GLOWING_REDSTONE_ORE.getId(), 0);
            }
            sender.sendMessage(ConfigurationService.GRAY + "  " + ConfigurationService.DOUBLEARROW + "  " + color + name + ": " + ConfigurationService.GRAY + amt);
        }
    }
}
