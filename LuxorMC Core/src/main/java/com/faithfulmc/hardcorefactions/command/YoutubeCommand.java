package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class YoutubeCommand implements CommandExecutor{
    private final int SUBSCRIBERS;
    private final int VIDEOS;
    private final int VIEWS;
    private final String[] message;

    public YoutubeCommand(HCF plugin) {
        Config config = new Config(plugin, "youtube");
        SUBSCRIBERS = config.getInt("subscribers", 3000);
        VIDEOS = config.getInt("videos", 2);
        VIEWS = config.getInt("views", 1000);
        message = new String[]{
                ConfigurationService.LINE_COLOR + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 55),
                ConfigurationService.GOLD + ChatColor.BOLD.toString() + "Youtube Requirements",
                ConfigurationService.GOLD + " * " + ConfigurationService.YELLOW + "You must have " + ConfigurationService.GOLD + ChatColor.BOLD + SUBSCRIBERS + " " + ConfigurationService.YELLOW + "subscribers.",
                ConfigurationService.GOLD + " * " + ConfigurationService.YELLOW + "You need to upload at least " + ConfigurationService.GOLD + ChatColor.BOLD + VIDEOS + ConfigurationService.YELLOW + " videos on the server.",
                ConfigurationService.GOLD + " * " + ConfigurationService.YELLOW + "You are required to have at least " + ConfigurationService.GOLD + ChatColor.BOLD + VIEWS + ConfigurationService.YELLOW + " views on recent videos.",
                ConfigurationService.LINE_COLOR + ChatColor.STRIKETHROUGH.toString() + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 55)
        };
    }


    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        sender.sendMessage(message);
        return true;
    }
}
