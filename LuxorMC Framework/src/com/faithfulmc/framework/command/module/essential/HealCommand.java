package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HealCommand extends BaseCommand {
    private static final Set<PotionEffectType> HEALING_REMOVEABLE_POTION_EFFECTS;

    static {
        HEALING_REMOVEABLE_POTION_EFFECTS = (Set) ImmutableSet.of((Object) PotionEffectType.SLOW, (Object) PotionEffectType.SLOW_DIGGING, (Object) PotionEffectType.POISON, (Object) PotionEffectType.WEAKNESS);
    }

    public HealCommand() {
        super("heal", "Heals a player.");
        this.setUsage("/(command) <playerName>");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        Player onlyTarget = null;
        ImmutableSet<Player> targets;
        if (args.length > 0 && sender.hasPermission(command.getPermission() + ".others")) {
            if (args[0].equalsIgnoreCase("all") && sender.hasPermission(command.getPermission() + ".all")) {
                targets = ImmutableSet.copyOf(Bukkit.getOnlinePlayers());
            } else {
                if ((onlyTarget = BukkitUtils.playerWithNameOrUUID(args[0])) == null || !BaseCommand.canSee(sender, onlyTarget)) {
                    sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
                    return true;
                }
                targets = ImmutableSet.of(onlyTarget);
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            targets = ImmutableSet.of((onlyTarget = (Player) sender));
        }
        for (Player target : targets) {
            target.setHealth((target).getMaxHealth());
            target.setFoodLevel(20);
            for (PotionEffectType type : HealCommand.HEALING_REMOVEABLE_POTION_EFFECTS) {
                target.removePotionEffect(type);
            }
        }
        Command.broadcastCommandMessage(sender, BaseConstants.YELLOW + "Healed " + ((onlyTarget == null) ? "all online players" : ("player " + onlyTarget.getName())) + '.');
        return true;
    }

    @Override
    public List onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return (args.length == 1) ? null : Collections.emptyList();
    }
}
