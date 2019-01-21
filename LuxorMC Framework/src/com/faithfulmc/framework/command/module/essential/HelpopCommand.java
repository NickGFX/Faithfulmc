package com.faithfulmc.framework.command.module.essential;

import com.comphenix.protocol.concurrency.ConcurrentPlayerMap;
import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.messgener.GlobalMessager;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class HelpopCommand extends BaseCommand {
    private final BasePlugin plugin;
    private final ConcurrentPlayerMap<Long> lastHelpop = new ConcurrentPlayerMap<Long>(ConcurrentPlayerMap.PlayerKey.ADDRESS);
    private final String prefix = BaseConstants.GOLD + "[Helpop] " + BaseConstants.YELLOW;
    private final long cooldown = TimeUnit.MINUTES.toMillis(1);

    public HelpopCommand(BasePlugin plugin) {
        super("helpop", "Request a staff members");
        this.plugin = plugin;
        setAliases(new String[]{"request","panic"});
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(prefix + "Invalid syntax: /" + s + " <message>");
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                long now = System.currentTimeMillis();
                long time = lastHelpop.getOrDefault(sender, 0L);
                if (time + cooldown > now) {
                    sender.sendMessage(prefix + BaseConstants.YELLOW + "You are on cooldown");
                    return false;
                }
                lastHelpop.put(player, now);
            }
            sender.sendMessage(prefix + ChatColor.GREEN + "Your helpop request has been submitted");
            String message = Joiner.on(" ").join(args);
            String id = plugin.getGlobalMessager().getId();
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BaseConstants.YELLOW + "Click to teleport"));
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + sender.getName());
            BaseComponent[] bukkitMSG = new ComponentBuilder("[Helpop] ").color(BaseConstants.fromBukkit(BaseConstants.GOLD)).event(hoverEvent).event(clickEvent).append(sender.getName()).color(BaseConstants.fromBukkit(BaseConstants.YELLOW)).append(" " + BaseConstants.DOUBLEARROW + " ").color(ChatColor.DARK_GRAY).append(message).color(BaseConstants.fromBukkit(BaseConstants.GOLD)).create();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(getPermission() + ".see")) {
                    player.spigot().sendMessage(bukkitMSG);
                }
            }
            hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BaseConstants.YELLOW + "Helpop from " + BaseConstants.GRAY + id));
            clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/play " + id);
            BaseComponent[] bungeeMSG = new ComponentBuilder("[Helpop] ").color(BaseConstants.fromBukkit(BaseConstants.GOLD)).event(hoverEvent).event(clickEvent).append("(" + id + ") ").color(BaseConstants.fromBukkit(BaseConstants.GRAY)).append(sender.getName()).color(BaseConstants.fromBukkit(BaseConstants.YELLOW)).append(" " + BaseConstants.DOUBLEARROW + " ").color(ChatColor.DARK_GRAY).append(message).color(BaseConstants.fromBukkit(BaseConstants.GOLD)).create();
            plugin.getGlobalMessager().broadcastToOtherServers(sender instanceof Player ? (Player) sender : GlobalMessager.getRandomPlayer(), bungeeMSG, "plugin.command.helpop.see");
        }
        return false;
    }
}
