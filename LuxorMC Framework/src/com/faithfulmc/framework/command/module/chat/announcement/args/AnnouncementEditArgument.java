package com.faithfulmc.framework.command.module.chat.announcement.args;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.announcement.Announcement;
import com.faithfulmc.framework.command.module.chat.announcement.AnnouncementCommand;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.primitives.Ints;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnouncementEditArgument extends CommandArgument{
    private final BasePlugin plugin;

    public AnnouncementEditArgument(BasePlugin plugin) {
        super("e", "Edit an announcement line");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return " (Utility Command) ";
    }

    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length >= 3){
            String announcementName = args[1].toLowerCase();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Announcement announcement = plugin.getAnnouncementManager().getAnnouncement(announcementName);
                if(args[2].startsWith("l")){
                    if(announcement == null){
                        commandSender.sendMessage(ChatColor.RED + "An announcement with that name does not exist");
                    }
                    else {
                        Bukkit.dispatchCommand(commandSender, AnnouncementCommand.LABEL + " lines " + announcementName);
                        String message = args.length >= 4 ? ChatColor.translateAlternateColorCodes('&', args[3]) : "";
                        for (int i = 4; i < args.length; i++) {
                            message += " " + ChatColor.translateAlternateColorCodes('&', args[i]);
                        }
                        int line = Ints.tryParse(args[2].substring(1, 2));
                        if(message.isEmpty()){
                            List<String> lines = new ArrayList<>(Arrays.asList(announcement.getLines()));
                            String previous = lines.remove(line);
                            announcement.setLines(lines.toArray(new String[lines.size()]));
                            commandSender.sendMessage(ChatColor.RED + "Removed " + ChatColor.GRAY + "[" + line + "] " + ChatColor.RESET + previous);
                        }
                        else {
                            commandSender.sendMessage(ChatColor.YELLOW + "Set line " + ChatColor.GRAY + "[" + line + "] " );
                            commandSender.sendMessage(ChatColor.RED + "- " + ChatColor.RESET + announcement.getLines()[line]);
                            announcement.getLines()[line] = message.isEmpty() ? null : message;
                            commandSender.sendMessage(ChatColor.GREEN + "+ " + ChatColor.RESET + message);
                        }
                        plugin.getAnnouncementManager().saveAnnouncement(announcement);
                    }
                }
                else if(args[2].startsWith("a")){
                    if(announcement == null){
                        commandSender.sendMessage(ChatColor.RED + "An announcement with that name does not exist");
                    }
                    else{
                        Bukkit.dispatchCommand(commandSender, AnnouncementCommand.LABEL + " lines " + announcementName);
                        String message = args.length >= 4 ? ChatColor.translateAlternateColorCodes('&', args[3]) : "";
                        for (int i = 4; i < args.length; i++) {
                            message += " " + ChatColor.translateAlternateColorCodes('&', args[i]);
                        }
                        int line = args[2].endsWith("-") ? -1 : Ints.tryParse(args[2].substring(1, 2));
                        List<String> lines = new ArrayList<>(Arrays.asList(announcement.getLines()));
                        lines.add(line + 1, message);
                        announcement.setLines(lines.toArray(new String[lines.size()]));
                        plugin.getAnnouncementManager().saveAnnouncement(announcement);
                    }
                }
                else if(args[2].equals("c")){
                    if(announcement == null){
                        Bukkit.dispatchCommand(commandSender, AnnouncementCommand.LABEL);
                        announcement = new Announcement(announcementName, new String[0], -1);
                        plugin.getAnnouncementManager().saveAnnouncement(announcement);
                        commandSender.sendMessage(ChatColor.YELLOW + "Created the announcement " + ChatColor.GOLD + announcementName);
                    }
                    else{
                        commandSender.sendMessage(ChatColor.RED + "An announcement with that name already exists");
                    }
                }
                else if(args[2].equals("r")){
                    if(announcement != null){
                        plugin.getAnnouncementManager().removeAnnouncement(announcement);
                        commandSender.sendMessage(ChatColor.YELLOW + "Removed the announcement " + ChatColor.GOLD + announcementName);
                    }
                    else{
                        commandSender.sendMessage(ChatColor.RED + "An announcement with that name does not exist");
                    }
                    Bukkit.dispatchCommand(commandSender, AnnouncementCommand.LABEL);
                }
                else if(args[2].startsWith("d")) {
                    if (announcement == null) {
                        commandSender.sendMessage(ChatColor.RED + "An announcement with that name does not exist");
                    } else {
                        Bukkit.dispatchCommand(commandSender, AnnouncementCommand.LABEL + " lines " + announcementName);
                        int line = Ints.tryParse(args[2].substring(1, 2));
                        if(line < announcement.getLines().length){
                            List<String> lines = new ArrayList<>(Arrays.asList(announcement.getLines()));
                            String replace = lines.remove(line);
                            commandSender.sendMessage(ChatColor.YELLOW + "Removed line " + ChatColor.GRAY + "[" + line + "] " );
                            commandSender.sendMessage(ChatColor.RED + "- " + ChatColor.RESET + replace);
                            announcement.setLines(lines.toArray(new String[lines.size()]));
                            plugin.getAnnouncementManager().saveAnnouncement(announcement);
                        }
                        else{
                            commandSender.sendMessage(ChatColor.RED + "Line not found");
                        }
                    }
                }
                else if(args[2].equals("b")){
                    plugin.getAnnouncementManager().sendBroadcastMessage(announcement.getLines());
                }
            });
        }
        else{
            commandSender.sendMessage(ChatColor.RED + "Usage: " + getUsage(AnnouncementCommand.LABEL));
        }
        return false;
    }
}
