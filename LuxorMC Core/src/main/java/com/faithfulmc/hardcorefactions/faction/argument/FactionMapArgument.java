package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.LandMap;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.hardcorefactions.visualise.VisualType;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.com.google.common.base.Enums;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionMapArgument extends CommandArgument {
    private final HCF plugin;

    public FactionMapArgument(final HCF plugin) {
        super("map", "View all claims around your chunk.");
        this.plugin = plugin;
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " [factionName]";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        final Player player = (Player) sender;
        final UUID uuid = player.getUniqueId();
        final FactionUser factionUser = this.plugin.getUserManager().getUser(uuid);
        VisualType visualType;
        if (args.length <= 1) {
            visualType = VisualType.CLAIM_MAP;
        } else if ((visualType = Enums.getIfPresent(VisualType.class, args[1]).orNull()) == null) {
            player.sendMessage(ConfigurationService.RED + "Visual miner " + args[1] + " not found.");
            return true;
        }
        final boolean newShowingMap = !factionUser.isShowClaimMap();
        if (newShowingMap) {
            if (!LandMap.updateMap(player, this.plugin, visualType, true)) {
                return true;
            }
        } else {
            this.plugin.getVisualiseHandler().clearVisualType(player, visualType, true);
            sender.sendMessage(ConfigurationService.RED + "Claim pillars are no longer shown.");
        }
        factionUser.setShowClaimMap(newShowingMap);
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }
        final VisualType[] values = VisualType.values();
        final List<String> results = new ArrayList<String>(values.length);
        for (final VisualType visualType : values) {
            results.add(visualType.name());
        }
        return results;
    }
}