package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class VanishCommand extends BaseCommand {
    private final BasePlugin plugin;

    public VanishCommand(final BasePlugin plugin) {
        super("vanish", "Hide from other players.");
        this.setAliases(new String[]{"v", "vis", "vanish", "invis"});
        this.setUsage("/(command) [playerName]");
        this.plugin = plugin;
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
        if (target != null && (!(sender instanceof Player) || ((Player) sender).canSee(target))) {
            final BaseUser baseUser = this.plugin.getUserManager().getUser(target.getUniqueId());
            final boolean newVanished = !baseUser.isVanished() || (args.length >= 1 && Boolean.parseBoolean(args[1]));
            baseUser.setVanished(target, newVanished, true);
            sender.sendMessage(BaseConstants.YELLOW + "Vanish mode of " + target.getName() + " set to " + newVanished + '.');
            return true;
        }
        sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }
}
