package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReclaimCommand implements CommandExecutor {
    private final File file;
    private final HCF hcf;
    private final List<ReclaimKit> reclaimKitList;
    private YamlConfiguration config;

    public ReclaimCommand(HCF hcf) {
        this.hcf = hcf;
        file = new File(hcf.getDataFolder(), "reclaim.yml");
        if (!file.exists()) {
            hcf.saveResource("reclaim.yml", false);
        }
        reclaimKitList = new ArrayList<>();
        try {
            config = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            handle(e);
            return;
        }
        for (String key : config.getKeys(false)) {
            String name = ChatColor.translateAlternateColorCodes('&', config.getString(key + ".name", ""));
            Object commandList = config.get(key + ".commands");
            List<String> commands;
            if (commandList instanceof List) {
                commands = new ArrayList<>();
                commands.addAll((List<String>) commandList);
            } else if (commandList instanceof String[]) {
                commands = new ArrayList<>();
                commands.addAll(Arrays.asList((String[]) commandList));
            } else {
                continue;
            }
            reclaimKitList.add(new ReclaimKit(name, "reclaim." + key, commands.toArray(new String[commands.size()])));
        }
    }

    public void handle(Exception ex) {
        ex.printStackTrace();
    }

    public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ReclaimKit chosenKit = null;
            for (ReclaimKit reclaimKit : reclaimKitList) {
                if (player.hasPermission(reclaimKit.getPermission())) {
                    chosenKit = reclaimKit;
                    break;
                }
            }
            if (chosenKit == null) {
                player.sendMessage(ConfigurationService.RED + "You do not have permisison to reclaim");
            } else {
                FactionUser factionUser = hcf.getUserManager().getUser(player.getUniqueId());
                if (factionUser.isReclaimed()) {
                    player.sendMessage(ConfigurationService.RED + "You have already reclaimed since SOTW");
                } else {
                    ConsoleCommandSender commandSender = Bukkit.getConsoleSender();
                    String playername = player.getName();
                    for (String cmd : chosenKit.getCommands()) {
                        Bukkit.dispatchCommand(commandSender, cmd.replace("{PLAYER}", playername).replace("{RANK}", chosenKit.getName()));
                    }
                    factionUser.setReclaimed(true);
                    player.sendMessage(ConfigurationService.RED + "You claimed loot for " + chosenKit.getName());
                }
            }
        } else {
            sender.sendMessage(ConfigurationService.RED + "You need to be a player to do this");
        }
        return true;
    }

    private class ReclaimKit {
        private final String name, permission;
        private final String[] commands;

        public ReclaimKit(String name, String permission, String[] commands) {
            this.name = name;
            this.permission = permission;
            this.commands = commands;
        }

        public String getName() {
            return name;
        }

        public String[] getCommands() {
            return commands;
        }

        public String getPermission() {
            return permission;
        }
    }
}
