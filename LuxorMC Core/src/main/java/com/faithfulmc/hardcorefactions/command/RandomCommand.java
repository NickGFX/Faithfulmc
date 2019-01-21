package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class RandomCommand implements org.bukkit.command.CommandExecutor {
    private final HCF plugin;

    public RandomCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }

        Player player = (Player) sender;
        List<Player> players = new ArrayList<>();
        for (Player players2 : org.bukkit.Bukkit.getOnlinePlayers()) {
            players.add(players2);
        }
        Collections.shuffle(players);
        Random random = new Random();
        int randoms = random.nextInt(Bukkit.getOnlinePlayers().size());
        Player p = players.get(randoms);
        if ((player.canSee(p)) && (player.hasPermission(command.getPermission() + ".teleport"))) {
            player.teleport(p);
            player.sendMessage(ConfigurationService.YELLOW + "You've teleported to " + p.getName());
        }
        else if (player.canSee(p)) {
            player.sendMessage(ConfigurationService.YELLOW + "You've found " + p.getName());
        } else {
            player.sendMessage(ConfigurationService.RED + "Player not found");
        }
        return true;

    }

}