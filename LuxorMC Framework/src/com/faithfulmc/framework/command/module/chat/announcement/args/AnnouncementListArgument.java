package com.faithfulmc.framework.command.module.chat.announcement.args;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.announcement.Announcement;
import com.faithfulmc.framework.command.module.chat.announcement.AnnouncementCommand;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnnouncementListArgument extends CommandArgument{
    private final BasePlugin plugin;

    public AnnouncementListArgument(BasePlugin plugin) {
        super("list", "List all announcements");
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return '/' + label+ ' ' + this.getName();
    }

    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
            commandSender.sendMessage(BaseConstants.GOLD + ChatColor.BOLD.toString() + "Announcements");
            commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
            List<Announcement> announcements = plugin.getAnnouncementManager().getAnnouncements();
            if(announcements.isEmpty()){
                commandSender.sendMessage(ChatColor.RED + "There are currently no announcements");
            }
            else{
                for(Announcement announcement: announcements){
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.fromLegacyText(
                                            ChatColor.GRAY + "Name: " + ChatColor.WHITE + WordUtils.capitalize(announcement.getName()) + "\n\n" +
                                            ChatColor.GRAY + "Delay: " + ChatColor.WHITE + (announcement.getDelay() <= 0 ? ChatColor.RED + "disabled" : DurationFormatUtils.formatDurationWords(TimeUnit.SECONDS.toMillis(announcement.getDelay()), true, true)) + "\n\n" +
                                            ChatColor.GRAY + "Announcement: " + ChatColor.WHITE + "\n" +
                                            Joiner.on("\n").join(announcement.getLines())

                            ));
                    BaseComponent[] baseComponents = new ComponentBuilder(" * ")
                            .color(ChatColor.DARK_GRAY)
                            .bold(true)
                            .event(hoverEvent)
                            .append(WordUtils.capitalize(announcement.getName()))
                            .color(ChatColor.YELLOW)
                            .event(hoverEvent)
                            .bold(false)
                            .append(" (Hover for information) ")
                            .color(ChatColor.GRAY)
                            .event(hoverEvent)
                            .append(" [Remove]")
                            .color(ChatColor.RED)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + "Click to remove this announcement")))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + AnnouncementCommand.LABEL + " e " + announcement.getName() + " r "))
                            .append(" [Lines]")
                            .color(ChatColor.GREEN)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + "Click to set the lines of this announcement")))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + AnnouncementCommand.LABEL + " lines " + announcement.getName()))
                            .append(" [Delay]")
                            .color(ChatColor.AQUA)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.AQUA + "Click to set the delay of this announcement")))
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + AnnouncementCommand.LABEL + " delay " + announcement.getName() + " "))
                            .append(" [Broadcast]")
                            .color(ChatColor.YELLOW)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + "Click to broadcast this announcement")))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + AnnouncementCommand.LABEL + " e " + announcement.getName() + " b"))
                            .create();
                    if(commandSender instanceof Player){
                        ((Player) commandSender).spigot().sendMessage(baseComponents);
                    }
                    else{
                        commandSender.sendMessage(TextComponent.toLegacyText(baseComponents));
                    }
                }
            }
            commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
            BaseComponent[] components = new ComponentBuilder("[Create Announcement]")
                    .color(ChatColor.GREEN)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.fromLegacyText(ChatColor.GREEN + "Click to create announcement")
                    ))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            "/" + AnnouncementCommand.LABEL + " e <name> c"
                    ))
                    .create();
            if(commandSender instanceof Player){
                ((Player) commandSender).spigot().sendMessage(components);
            }
            else{
                commandSender.sendMessage(TextComponent.toLegacyText(components));
            }
            commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
        });
        return false;
    }
}
