package com.faithfulmc.hardcorefactions.kit;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.kit.argument.*;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.command.ArgumentExecutor;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KitExecutor extends ArgumentExecutor {
    private final HCF plugin;

    public KitExecutor(final HCF plugin) {
        super("kit");
        this.plugin = plugin;
        this.addArgument(new KitApplyArgument(plugin));
        this.addArgument(new KitCreateArgument(plugin));
        this.addArgument(new KitDeleteArgument(plugin));
        this.addArgument(new KitSetDescriptionArgument(plugin));
        this.addArgument(new KitDisableArgument(plugin));
        this.addArgument(new KitGuiArgument(plugin));
        this.addArgument(new KitListArgument(plugin));
        this.addArgument(new KitPreviewArgument(plugin));
        this.addArgument(new KitRenameArgument(plugin));
        this.addArgument(new KitSetDelayArgument(plugin));
        this.addArgument(new KitSetImageArgument(plugin));
        this.addArgument(new KitSetIndexArgument(plugin));
        this.addArgument(new KitSetItemsArgument(plugin));
        this.addArgument(new KitSetMaxUsesArgument(plugin));
        this.addArgument(new KitSetminplaytimeArgument(plugin));
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if(!sender.isOp()){
            Bukkit.dispatchCommand(sender, "gkits");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ConfigurationService.GOLD + "*** Kit Help ***");
            for (final CommandArgument permission21 : this.arguments) {
                final String kit1 = permission21.getPermission();
                if (kit1 == null || sender.hasPermission(kit1)) {
                    sender.sendMessage(ConfigurationService.SCOREBOARD_COLOR + permission21.getUsage(label) + " - " + permission21.getDescription() + '.');
                }
            }
            sender.sendMessage(ConfigurationService.SCOREBOARD_COLOR + "/" + label + " <kitName> - Applies a kit.");
            return true;
        }
        final CommandArgument argument22 = this.getArgument(args[0]);
        final String permission22 = (argument22 == null) ? null : argument22.getPermission();
        if (argument22 == null || (permission22 != null && !sender.hasPermission(permission22))) {
            final Kit kit2 = this.plugin.getKitManager().getKit(args[0]);
            if (sender instanceof Player && kit2 != null) {
                final String kitPermission = kit2.getPermissionNode();
                if (kitPermission == null || sender.hasPermission(kitPermission)) {
                    final Player player = (Player) sender;
                    kit2.applyTo(player, false, true);
                    return true;
                }
            }
            sender.sendMessage(ConfigurationService.RED + "Kit sub-command or kit " + args[0] + " not found.");
            return true;
        }
        argument22.onCommand(sender, command, label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 1) {
            return super.onTabComplete(sender, command, label, args);
        }
        final List<String> previous = super.onTabComplete(sender, command, label, args);
        final List<String> kitNames = new ArrayList<>();
        for (final Kit kit : this.plugin.getKitManager().getKits()) {
            final String permission = kit.getPermissionNode();
            if (permission == null || sender.hasPermission(permission)) {
                kitNames.add(kit.getName());
            }
        }
        List<String> previous2;
        if (previous != null && !previous.isEmpty()) {
            previous2 = new ArrayList<>(previous);
            previous2.addAll(0, kitNames);
        } else {
            previous2 = kitNames;
        }
        return BukkitUtils.getCompletions(args, previous2);
    }
}
