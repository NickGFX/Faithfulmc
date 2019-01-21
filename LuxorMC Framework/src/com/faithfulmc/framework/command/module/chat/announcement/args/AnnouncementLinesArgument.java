package com.faithfulmc.framework.command.module.chat.announcement.args;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.announcement.Announcement;
import com.faithfulmc.framework.command.module.chat.announcement.AnnouncementCommand;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.command.CommandArgument;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementLinesArgument extends CommandArgument{
    private final BasePlugin plugin;
    
    public AnnouncementLinesArgument(BasePlugin plugin) {
        super("lines", "Displays lines from an announcement");
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "/" + label + " " + getName() + " <announcement>";
    }

    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length >= 2){
            String announcementName = args[1].toLowerCase();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Announcement announcement = plugin.getAnnouncementManager().getAnnouncement(announcementName);
                if(announcement == null){
                    commandSender.sendMessage(ChatColor.RED + "Announcement not found");
                }
                else{
                    commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
                    commandSender.sendMessage(BaseConstants.GOLD + ChatColor.BOLD.toString() + "Announcement - " + WordUtils.capitalize(announcementName));
                    commandSender.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 45));
                    commandSender.sendMessage(BaseConstants.YELLOW + ChatColor.BOLD.toString() + "Lines: ");
                    List<BaseComponent[]> messages = new ArrayList<>();
                    messages.add(new ComponentBuilder("[Add]")
                            .color(ChatColor.GREEN)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    TextComponent.fromLegacyText(ChatColor.GREEN + "Click to add at line " + ChatColor.GRAY + "[" + 0 + "]")
                            ))
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                    "/" + AnnouncementCommand.LABEL + " e " + announcementName + " a- "
                            ))
                            .create());
                    int i = 0;
                    for(String line: announcement.getLines()) {
                        messages.add(new ComponentBuilder("[" + i + "]")
                                .color(ChatColor.GRAY)
                                .append(" ")
                                .append(line)
                                .append(" ")
                                .append("[Delete] ")
                                .color(ChatColor.RED)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        TextComponent.fromLegacyText(ChatColor.RED + "Click to delete line " + ChatColor.GRAY + i)
                                ))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/" + AnnouncementCommand.LABEL + " e " + announcementName + " d" + i
                                ))
                                .append("[Edit] ")
                                .color(ChatColor.AQUA)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        TextComponent.fromLegacyText(ChatColor.AQUA + "Click to edit line " + ChatColor.GRAY + i)
                                ))
                                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                        "/" + AnnouncementCommand.LABEL + " e " + announcementName + " l" + i + " " + announcement.getLines()[i].replace(String.valueOf(ChatColor.COLOR_CHAR), "&")
                                ))
                                .append("[Add] ")
                                .color(ChatColor.GREEN)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        TextComponent.fromLegacyText(ChatColor.GREEN + "Click to add a line after " + ChatColor.GRAY + "[" + i + "]")
                                ))
                                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                        "/" + AnnouncementCommand.LABEL + " e " + announcementName + " a" + i + " "
                                ))
                                .create());
                        i++;
                    }
                    for(BaseComponent[] baseComponents: messages){
                        if(commandSender instanceof Player){
                            ((Player) commandSender).spigot().sendMessage(baseComponents);
                        }
                        else{
                            commandSender.sendMessage(TextComponent.toLegacyText(baseComponents));
                        }
                    }
                }
            });
        }
        else{
            commandSender.sendMessage(ChatColor.RED + "Usage: " + getUsage(AnnouncementCommand.LABEL));

        }
        return false;
    }
}
