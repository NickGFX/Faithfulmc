package com.faithfulmc.framework.command.module.inventory;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.StaffPriority;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CopyInvCommand extends BaseCommand {
    public CopyInvCommand() {
        super("copyinv", "Copies a players inv");
        this.setAliases(new String[]{"copyinventory"});
        this.setUsage("/(command) <playerName>");
    }

    @Override
    public boolean onCommand(final CommandSender cs, final Command cmd, final String s, final String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(ChatColor.RED + "Error, your not a player ;)");
            return true;
        }
        if (args.length == 0) {
            cs.sendMessage(ChatColor.RED + this.getUsage());
            return true;
        }
        final Player player = (Player) cs;
        if (args.length == 1) {
            final Player target = BukkitUtils.playerWithNameOrUUID(args[0]);
            if (target == null || !BaseCommand.canSee((CommandSender) player, target)) {
                player.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
                return true;
            }
            final StaffPriority selfPriority = StaffPriority.of(player);
            if (selfPriority != StaffPriority.ADMIN && StaffPriority.of(target).isMoreThan(selfPriority)) {
                cs.sendMessage(ChatColor.RED + "You do not have access to check the inventory of that player.");
                return true;
            }
            player.getInventory().setContents(target.getInventory().getContents());
            player.getInventory().setArmorContents(target.getInventory().getArmorContents());
            player.sendMessage(BaseConstants.YELLOW + "You have copied the inventory of " + target.getName());
        }
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return null;
    }
}
