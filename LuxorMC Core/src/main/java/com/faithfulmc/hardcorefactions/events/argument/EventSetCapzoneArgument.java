package com.faithfulmc.hardcorefactions.events.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.faction.CapturableFaction;
import com.faithfulmc.hardcorefactions.events.faction.CitadelFaction;
import com.faithfulmc.hardcorefactions.events.faction.ConquestFaction;
import com.faithfulmc.hardcorefactions.events.faction.KothFaction;
import com.faithfulmc.hardcorefactions.events.tracker.CitadelTracker;
import com.faithfulmc.hardcorefactions.events.tracker.ConquestTracker;
import com.faithfulmc.hardcorefactions.events.tracker.KothTracker;
import com.faithfulmc.hardcorefactions.faction.FactionManager;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.command.CommandArgument;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EventSetCapzoneArgument extends CommandArgument {
    private final HCF plugin;

    public EventSetCapzoneArgument(final HCF plugin) {
        super("setcapzone", "Sets the capture zone of an event");
        this.plugin = plugin;
        this.aliases = new String[]{"setcapturezone", "setcap", "setcappoint", "setcapturepoint", "setcappoint"};
        this.permission = "hcf.command.event.argument." + this.getName();
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <eventName>";
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "Only players can set KOTH arena capture points");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ConfigurationService.RED + "Usage: " + this.getUsage(label));
            return true;
        }
        final WorldEditPlugin worldEdit = this.plugin.getWorldEdit();
        if (worldEdit == null) {
            sender.sendMessage(ConfigurationService.RED + "WorldEdit must be installed to set KOTH capture points.");
            return true;
        }
        final Selection selection = worldEdit.getSelection((Player) sender);
        if (selection == null) {
            sender.sendMessage(ConfigurationService.RED + "You must make a WorldEdit selection to do this.");
            return true;
        }
        if (selection.getWidth() < 2 || selection.getLength() < 2) {
            sender.sendMessage(ConfigurationService.RED + "Capture zones must be at least " + 2 + 'x' + 2 + '.');
            return true;
        }
        final Faction faction = this.plugin.getFactionManager().getFaction(args[1]);
        if (!(faction instanceof CapturableFaction)) {
            sender.sendMessage(ConfigurationService.RED + "There is not a capturable faction named '" + args[1] + "'.");
            return true;
        }
        final CapturableFaction capturableFaction = (CapturableFaction) faction;
        final Collection<Claim> claims = capturableFaction.getClaims();
        if (claims.isEmpty()) {
            sender.sendMessage(ConfigurationService.RED + "Capture zones can only be inside the event claim.");
            return true;
        }
        final Claim claim = new Claim(faction, selection.getMinimumPoint(), selection.getMaximumPoint());
        final World world = claim.getWorld();
        final int minimumX = claim.getMinimumX();
        final int maximumX = claim.getMaximumX();
        final int minimumZ = claim.getMinimumZ();
        final int maximumZ = claim.getMaximumZ();
        final FactionManager factionManager = this.plugin.getFactionManager();
        for (int x = minimumX; x <= maximumX; ++x) {
            for (int z = minimumZ; z <= maximumZ; ++z) {
                final Faction factionAt = factionManager.getFactionAt(world, x, z);
                if (!factionAt.equals(capturableFaction)) {
                    sender.sendMessage(ConfigurationService.RED + "Capture zones can only be inside the event claim.");
                    return true;
                }
            }
        }
        CaptureZone captureZone;
        if (capturableFaction instanceof ConquestFaction) {
            if (args.length < 3) {
                sender.sendMessage(ConfigurationService.RED + "Usage: /" + label + ' ' + this.getName() + ' ' + faction.getName() + " <red|blue|green|yellow|bonus>");
                return true;
            }
            final ConquestFaction conquestFaction = (ConquestFaction) capturableFaction;
            final ConquestFaction.ConquestZone conquestZone = ConquestFaction.ConquestZone.getByName(args[2]);
            if (conquestZone == null) {
                sender.sendMessage(ConfigurationService.RED + "There is no conquest zone named '" + args[2] + "'.");
                sender.sendMessage(ConfigurationService.RED + "Did you mean?: " + StringUtils.join((Iterable) ConquestFaction.ConquestZone.getNames(), ", "));
                return true;
            }
            captureZone = new CaptureZone(conquestZone.getName(), conquestZone.getColor().toString(), claim, ConquestTracker.DEFAULT_CAP_MILLIS);
            conquestFaction.setZone(conquestZone, captureZone);
        } else {
            ((KothFaction) capturableFaction).setCaptureZone(captureZone = new CaptureZone(capturableFaction.getName(), claim, capturableFaction instanceof CitadelFaction ? CitadelTracker.DEFAULT_CAP_MILLIS : KothTracker.DEFAULT_CAP_MILLIS));
        }
        sender.sendMessage(ConfigurationService.YELLOW + "Set capture zone " + captureZone.getDisplayName() + ConfigurationService.YELLOW + " for faction " + faction.getName() + ConfigurationService.YELLOW + '.');
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        switch (args.length) {
            case 3: {
                final Faction faction2 = this.plugin.getFactionManager().getFaction(args[1]);
                if (faction2 instanceof ConquestFaction) {
                    final ConquestFaction.ConquestZone[] zones = ConquestFaction.ConquestZone.values();
                    final List<String> results = new ArrayList<String>(zones.length);
                    for (final ConquestFaction.ConquestZone zone : zones) {
                        results.add(zone.name());
                    }
                    return results;
                }
                return Collections.emptyList();
            }
            default: {
                return Collections.emptyList();
            }
        }
    }
}