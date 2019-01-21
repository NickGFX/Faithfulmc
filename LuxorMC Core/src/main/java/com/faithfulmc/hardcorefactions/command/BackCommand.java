package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.PersistableLocation;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collections;
import java.util.List;

public class BackCommand extends BaseCommand implements Listener {
    private final HCF plugin;

    public BackCommand(final HCF plugin) {
        super("back", "Go to a players last known location.");
        this.setUsage("/(command) [playerName]");
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        Player target;
        if (args.length > 0 && sender.hasPermission(command.getPermission() + ".others")) {
            target = BukkitUtils.playerWithNameOrUUID(args[0]);
            if (target == null) {
                sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
                return true;
            }
        } else {
            target = (Player) sender;
        }
        final FactionUser targetUser = this.plugin.getUserManager().getUser(target.getUniqueId());
        final Location previous = targetUser.getBackLocation().getLocation().clone();
        if (previous == null) {
            sender.sendMessage(ConfigurationService.RED + target.getName() + " doesn't have a back location.");
            return true;
        }
        ((Player) sender).teleport(previous);
        sender.sendMessage(ConfigurationService.YELLOW + "Teleported to back location of " + target.getName() + '.');
        return true;
    }

    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        this.plugin.getUserManager().getUser(player.getUniqueId()).setBackLocation(new PersistableLocation(player.getLocation().clone()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            this.plugin.getUserManager().getUser(event.getPlayer().getUniqueId()).setBackLocation(new PersistableLocation(event.getFrom().clone()));
        }
    }
}
