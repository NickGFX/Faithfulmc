package com.faithfulmc.framework.command.module.chat.announcement.args;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.announcement.Announcement;
import com.faithfulmc.framework.command.module.chat.announcement.AnnouncementCommand;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnnouncementDelayArgument extends CommandArgument{
    private final BasePlugin plugin;

    public AnnouncementDelayArgument(BasePlugin plugin) {
        super("delay", "Sets the delay of an announcements");
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "/" + label + " " + getName() + " <announcement> <delay>";
    }

    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length >= 3){
            String announcementName = args[1].toLowerCase();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Announcement announcement = plugin.getAnnouncementManager().getAnnouncement(announcementName);
                if(announcement == null){
                    commandSender.sendMessage(ChatColor.RED + "Announcement not found");
                }
                else{
                    int delay = -1;
                    try{
                        delay = (int) TimeUnit.MILLISECONDS.toSeconds(JavaUtils.parse(args[2]));
                    }
                    catch (NumberFormatException exception){
                        commandSender.sendMessage(ChatColor.RED + "Invalid number, disabling announcement");
                    }
                    announcement.setDelay(delay);
                    plugin.getAnnouncementManager().saveAnnouncement(announcement);

                    Bukkit.dispatchCommand(commandSender, AnnouncementCommand.LABEL);

                    commandSender.sendMessage(ChatColor.YELLOW + "Set the delay of " + ChatColor.GOLD + WordUtils.capitalize(announcementName) + ChatColor.YELLOW + " to " + ChatColor.GRAY + (delay <= 0 ? ChatColor.RED + "disabled" : DurationFormatUtils.formatDurationWords(TimeUnit.SECONDS.toMillis(delay), true, true)));
                }
            });
        }
        else{
            commandSender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));

        }
        return false;
    }
}
