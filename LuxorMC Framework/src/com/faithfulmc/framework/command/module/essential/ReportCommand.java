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

public class ReportCommand extends BaseCommand {
    private final BasePlugin plugin;
    private final ConcurrentPlayerMap<Long> lastReport = new ConcurrentPlayerMap<Long>(ConcurrentPlayerMap.PlayerKey.ADDRESS);
    private final String prefix = ChatColor.RED + "[Report] " + BaseConstants.YELLOW;
    private final long cooldown = TimeUnit.MINUTES.toMillis(1);

    public ReportCommand(BasePlugin plugin) {
        super("report", "Report a player");
        this.plugin = plugin;
        setAliases(new String[]{"hacker"});
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(prefix + "Invalid syntax: /" + s + " <player> <message>");
        } else {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                long now = System.currentTimeMillis();
                long time = lastReport.getOrDefault(sender, 0L);
                if (time + cooldown > now) {
                    sender.sendMessage(prefix + "You are on cooldown");
                    return false;
                }
                lastReport.put(player, now);
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(prefix + "That player is not online");
            } else {
                String id = plugin.getGlobalMessager().getId();
                sender.sendMessage(prefix + ChatColor.GREEN + "Your report has been submitted");
                String message = Joiner.on(" ").join(args).substring(args[0].length() + 1);
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BaseConstants.YELLOW + "Click to teleport"));
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + target.getName());
                BaseComponent[] bukkitMSG = new ComponentBuilder("[Report] ").color(ChatColor.DARK_RED).event(hoverEvent).event(clickEvent).append(target.getName()).color(BaseConstants.fromBukkit(BaseConstants.YELLOW)).append(" " + BaseConstants.DOUBLEARROW + " ").color(ChatColor.DARK_GRAY).append(message).color(BaseConstants.fromBukkit(BaseConstants.GOLD)).create();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission(getPermission() + ".see")) {
                        player.spigot().sendMessage(bukkitMSG);
                    }
                }
                hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(BaseConstants.YELLOW + "Report from " + BaseConstants.GRAY + id));
                clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/play " + id);
                BaseComponent[] bungeeMSG = new ComponentBuilder("[Report] ").color(ChatColor.DARK_RED).event(hoverEvent).event(clickEvent).append("(" + id + ") ").color(BaseConstants.fromBukkit(BaseConstants.GRAY)).append(target.getName()).color(BaseConstants.fromBukkit(BaseConstants.YELLOW)).append(" " + BaseConstants.DOUBLEARROW + " ").color(ChatColor.DARK_GRAY).append(message).color(BaseConstants.fromBukkit(BaseConstants.GOLD)).create();
                plugin.getGlobalMessager().broadcastToOtherServers(sender instanceof Player ? (Player) sender : GlobalMessager.getRandomPlayer(), bungeeMSG, "plugin.command.report.see");
            }
        }
        return false;
    }


}
