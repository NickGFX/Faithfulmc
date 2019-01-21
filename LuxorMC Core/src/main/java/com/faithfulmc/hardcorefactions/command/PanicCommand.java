package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.framework.command.module.essential.FreezeCommand;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class PanicCommand implements CommandExecutor{
    private final HCF plugin;

    private final long COOLDOWN = TimeUnit.HOURS.toMillis(1);

    public PanicCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
            if(factionUser != null){
                long now = System.currentTimeMillis();
                long lastPanic = factionUser.getLastPanic();
                long diff = now - lastPanic;
                if(diff > COOLDOWN){
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GRAY + "Teleport to " + ChatColor.WHITE + player.getName()));
                    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName());
                    BaseComponent[] baseComponents = new ComponentBuilder("[Panic] ").event(hoverEvent).event(clickEvent).color(ChatColor.RED).bold(true).append(player.getName()).bold(false).color(ChatColor.WHITE).append(" has used the panic command").color(ChatColor.GRAY).create();
                    plugin.getLogger().info(TextComponent.toPlainText(baseComponents));
                    new BukkitRunnable() {
                        public void run(){
                            if(player.isOnline() && FreezeCommand.isFrozen(player.getUniqueId())) {
                                for (Player other : Bukkit.getOnlinePlayers()) {
                                    if (other.hasPermission("base.command.staffchat")) {
                                        other.spigot().sendMessage(baseComponents);
                                    }
                                }
                            }
                            else{
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 0, 20 * 30);
                    factionUser.setLastPanic(now);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ss " + player.getName());
                }
                else{
                    sender.sendMessage(ChatColor.YELLOW + "You must wait " + ChatColor.GRAY + DurationFormatUtils.formatDurationWords(COOLDOWN - diff, true, true) + ChatColor.YELLOW + " before panicking again.");
                }
            }
        }
        else{
            sender.sendMessage(ChatColor.RED + "You need to be a player to do this");
        }
        return true;
    }
}
