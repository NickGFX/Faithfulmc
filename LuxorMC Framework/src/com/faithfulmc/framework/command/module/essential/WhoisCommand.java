package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.StaffPriority;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WhoisCommand extends BaseCommand {
    private static final Map CLIENT_PROTOCOL_IDS;

    static {
        CLIENT_PROTOCOL_IDS = ImmutableMap.of(4, "1.7.2 -> 1.7.5", 5, "1.7.6 -> 1.7.10", 47, "1.8 -> 1.8.8");
    }

    private final BasePlugin plugin;

    public WhoisCommand(final BasePlugin plugin) {
        super("whois", "Check information about a player.");
        this.plugin = plugin;
        this.setUsage("/(command) [playerName]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage());
            return true;
        }
        final Player target = BukkitUtils.playerWithNameOrUUID(args[0]);
        if (target != null && BaseCommand.canSee(sender, target)) {
            final Location location = target.getLocation();
            final World world = location.getWorld();
            final BaseUser baseUser = this.plugin.getUserManager().getUser(target.getUniqueId());
            sender.sendMessage(BaseConstants.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            sender.sendMessage(ChatColor.GREEN + " [" + target.getDisplayName() + ChatColor.GREEN + ']');
            sender.sendMessage(BaseConstants.YELLOW + "  Health: " + BaseConstants.GOLD + (target).getHealth() + '/' + (target).getMaxHealth());
            sender.sendMessage(BaseConstants.YELLOW + "  Hunger: " + BaseConstants.GOLD + target.getFoodLevel() + '/' + 20 + " (" + target.getSaturation() + " saturation)");
            sender.sendMessage(BaseConstants.YELLOW + "  Exp/Level: " + BaseConstants.GOLD + target.getExp() + '/' + target.getLevel());
            sender.sendMessage(BaseConstants.YELLOW + "  Location: " + BaseConstants.GOLD + world.getName() + ' ' + BaseConstants.GRAY + '[' + WordUtils.capitalizeFully(world.getEnvironment().name().replace('_', ' ')) + "] " + BaseConstants.GOLD + '(' + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ')');
            sender.sendMessage(BaseConstants.YELLOW + "  Vanished: " + BaseConstants.GOLD + baseUser.isVanished() + " (priority=" + StaffPriority.of(target).getPriorityLevel() + ')');
            sender.sendMessage(BaseConstants.YELLOW + "  Staff Chat: " + BaseConstants.GOLD + baseUser.isInStaffChat());
            sender.sendMessage(BaseConstants.YELLOW + "  Operator: " + BaseConstants.GOLD + target.isOp());
            sender.sendMessage(BaseConstants.YELLOW + "  Game Mode: " + BaseConstants.GOLD + WordUtils.capitalizeFully(target.getGameMode().name().replace('_', ' ')));
            sender.sendMessage(BaseConstants.YELLOW + "  Idle Time: " + BaseConstants.GOLD + DurationFormatUtils.formatDurationWords(BukkitUtils.getIdleTime(target), true, true));
            sender.sendMessage(BaseConstants.YELLOW + "  IP Address: " + BaseConstants.GOLD + target.getAddress().getHostString());
            int version = BasePlugin.getPlugin().getNmsProvider().getVersion(target);
            sender.sendMessage(BaseConstants.YELLOW + "  Client Version: " + BaseConstants.GOLD + version + BaseConstants.GRAY + " [" + MoreObjects.firstNonNull(WhoisCommand.CLIENT_PROTOCOL_IDS.get(version), (Object) "Unknown (check at http://wiki.vg/Protocol_version_numbers)") + "]");
            sender.sendMessage(BaseConstants.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
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
