package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;
import java.util.List;

public class KillCommand extends BaseCommand {
    public KillCommand() {
        super("kill", "Kills a player.");
        this.setAliases(new String[]{"slay"});
        this.setUsage("/(command) <playerName>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        Player target;
        if (args.length > 0 && sender.hasPermission(command.getPermission() + ".others")) {
            target = BukkitUtils.playerWithNameOrUUID(args[0]);
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            target = (Player) sender;
        }
        if (target == null || !BaseCommand.canSee(sender, target)) {
            sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
            return true;
        }
        if (target.isDead()) {
            sender.sendMessage(ChatColor.RED + target.getName() + " is already dead.");
            return true;
        }
        final EntityDamageEvent event = new EntityDamageEvent((Entity) target, EntityDamageEvent.DamageCause.SUICIDE, 10000);
        Bukkit.getPluginManager().callEvent((Event) event);
        if (event.isCancelled()) {
            sender.sendMessage(ChatColor.RED + "You cannot kill " + target.getName() + '.');
            return true;
        }
        target.setLastDamageCause(event);
        target.setHealth(0.0);
        if (sender.equals(target)) {
            sender.sendMessage(BaseConstants.GOLD + "You have been killed.");
            return true;
        }
        Command.broadcastCommandMessage(sender, BaseConstants.YELLOW + "Slain player " + target.getName() + '.');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1 && sender.hasPermission(command.getPermission() + ".others")) ? null : Collections.emptyList();
    }
}
