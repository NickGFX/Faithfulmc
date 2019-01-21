package com.faithfulmc.framework.command.module.chat;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ToggleSoundsCommand extends BaseCommand implements Listener {
    private final BasePlugin plugin;

    public ToggleSoundsCommand(final BasePlugin plugin) {
        super("sounds", "Toggles messaging sounds.");
        this.setAliases(new String[]{"pmsounds", "togglepmsounds", "messagingsounds"});
        this.setUsage("/(command) [playerName]");
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents((Listener) this, (Plugin) plugin);
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }
        final Player player = (Player) sender;
        final BaseUser baseUser = this.plugin.getUserManager().getUser(player.getUniqueId());
        final boolean newMessagingSounds = !baseUser.isMessagingSounds() || (args.length >= 2 && Boolean.parseBoolean(args[1]));
        baseUser.setMessagingSounds(newMessagingSounds);
        sender.sendMessage(BaseConstants.YELLOW + "Messaging sounds are now " + (newMessagingSounds ? (ChatColor.GREEN + "on") : (ChatColor.RED + "off")) + BaseConstants.YELLOW + '.');
        return true;
    }
}
