package com.faithfulmc.hardcorefactions.faction.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.type.StuckTimer;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class FactionStuckArgument extends CommandArgument {
    private final HCF plugin;


    public FactionStuckArgument(HCF plugin) {
        super("stuck", "Teleport to a safe position.", new String[]{"trap", "trapped"});
        this.plugin = plugin;

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName();

    }


    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");

            return true;

        }

        Player player = (Player) sender;

        if (player.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL) {

            sender.sendMessage(ConfigurationService.RED + "You can only use this command from the overworld.");

            return true;

        }

        StuckTimer stuckTimer = this.plugin.getTimerManager().stuckTimer;

        if (!stuckTimer.setCooldown(player, player.getUniqueId())) {

            sender.sendMessage(ConfigurationService.RED + "Your " + stuckTimer.getDisplayName() + ConfigurationService.RED + " timer is already active.");

            return true;

        }

        sender.sendMessage(ConfigurationService.YELLOW + stuckTimer.getDisplayName() + ConfigurationService.YELLOW + " timer has started. " + "Teleportation will commence in " + ChatColor.LIGHT_PURPLE + HCF.getRemaining(stuckTimer.getRemaining(player), true, false) + ConfigurationService.YELLOW + ". " + "This will cancel if you move more than " + 5 + " blocks.");

        return true;

    }

}