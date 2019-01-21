package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GamemodeCommand extends BaseCommand {
    public GamemodeCommand() {
        super("gamemode", "Sets a gamemode for a player.");
        this.setAliases(new String[]{"gm"});
        this.setUsage("/(command) <modeName> [playerName]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final GameMode mode = this.getGameModeByName(args[0]);
        if (mode == null) {
            sender.sendMessage(ChatColor.RED + "Gamemode '" + args[0] + "' not found.");
            return true;
        }
        Player target;
        if (args.length > 1) {
            if (sender.hasPermission(command.getPermission() + ".others")) {
                target = BukkitUtils.playerWithNameOrUUID(args[1]);
            } else {
                target = null;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            target = (Player) sender;
        }
        if (target == null || !BaseCommand.canSee(sender, target)) {
            sender.sendMessage(BaseConstants.GOLD + "Player named or with UUID '" + ChatColor.WHITE + args[1] + BaseConstants.GOLD + "' not found.");
            return true;
        }
        if (target.getGameMode() == mode) {
            sender.sendMessage(ChatColor.RED + "Gamemode of " + target.getName() + " is already " + mode.name() + '.');
            return true;
        }
        target.setGameMode(mode);
        Command.broadcastCommandMessage(sender, BaseConstants.GOLD + "Set gamemode of " + target.getName() + BaseConstants.GOLD + " to " + ChatColor.WHITE + mode.name() + BaseConstants.GOLD + '.');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        final GameMode[] gameModes = GameMode.values();
        final ArrayList results = new ArrayList(gameModes.length);
        final GameMode[] var7 = gameModes;
        for (int var8 = gameModes.length, var9 = 0; var9 < var8; ++var9) {
            final GameMode mode = var7[var9];
            results.add(mode.name());
        }
        return BukkitUtils.getCompletions(args, results);
    }

    private GameMode getGameModeByName(String id) {
        id = id.toLowerCase(Locale.ENGLISH);
        return (!id.equalsIgnoreCase("gmc") && !id.contains("creat") && !id.equalsIgnoreCase("1") && !id.equalsIgnoreCase("c")) ? ((!id.equalsIgnoreCase("gms") && !id.contains("survi") && !id.equalsIgnoreCase("0") && !id.equalsIgnoreCase("s")) ? ((!id.equalsIgnoreCase("gma") && !id.contains("advent") && !id.equalsIgnoreCase("2") && !id.equalsIgnoreCase("a")) ? ((!id.equalsIgnoreCase("gmt") && !id.contains("toggle") && !id.contains("cycle") && !id.equalsIgnoreCase("t")) ? null : null) : GameMode.ADVENTURE) : GameMode.SURVIVAL) : GameMode.CREATIVE;
    }
}
