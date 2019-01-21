package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SudoCommand extends BaseCommand {
    public SudoCommand() {
        super("sudo", "Forces a player to run command.");
        this.setUsage("/(command) <force> <all|playerName> <command args...> \n[Warning!] Forcing will give player temporary OP until executed.");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage());
            return true;
        }
        boolean force;
        try {
            force = Boolean.parseBoolean(args[0]);
        } catch (IllegalArgumentException var9) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage());
            return true;
        }
        final String executingCommand = StringUtils.join((Object[]) args, ' ', 2, args.length);
        if (args[1].equalsIgnoreCase("all")) {
            for (Player target3 : Bukkit.getOnlinePlayers()) {
                this.executeCommand(target3, executingCommand, force);
            }
            sender.sendMessage(ChatColor.RED + "Forcing all players to run " + executingCommand + (force ? " with permission bypasses" : "") + '.');
            return true;
        }
        final Player target4 = Bukkit.getPlayer(args[1]);
        if (target4 != null && BaseCommand.canSee(sender, target4)) {
            this.executeCommand(target4, executingCommand, force);
            Command.broadcastCommandMessage(sender, ChatColor.RED + sender.getName() + ChatColor.RED + " made " + target4.getName() + " run " + executingCommand + (force ? " with permission bypasses" : "") + '.');
            sender.sendMessage(ChatColor.RED + "Making " + target4.getName() + " to run " + executingCommand + (force ? " with permission bypasses" : "") + '.');
            return true;
        }
        sender.sendMessage(BaseConstants.GOLD + "Player named or with UUID '" + ChatColor.WHITE + args[1] + BaseConstants.GOLD + "' not found.");
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 1) {
            final ArrayList results = new ArrayList(2);
            results.add("true");
            results.add("false");
            return BukkitUtils.getCompletions(args, results);
        }
        if (args.length != 2) {
            return Collections.emptyList();
        }
        final ArrayList results = new ArrayList();
        results.add("ALL");
        final Player senderPlayer = (sender instanceof Player) ? (Player) sender : null;
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (senderPlayer == null || senderPlayer.canSee(target)) {
                results.add(target.getName());
            }
        }
        return BukkitUtils.getCompletions(args, results);
    }

    private boolean executeCommand(final Player target, final String executingCommand, boolean force) {
        if (target.isOp()) {
            force = false;
        }
        boolean var5;
        try {
            if (force) {
                target.setOp(true);
            }
            target.performCommand(executingCommand);
            final boolean ex = true;
            return ex;
        } catch (Exception var6) {
            var5 = false;
        } finally {
            if (force) {
                target.setOp(false);
            }
        }
        return var5;
    }
}
