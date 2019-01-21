package com.faithfulmc.hardcorefactions.events.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.faction.KothFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import com.google.common.primitives.Ints;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventSetCaptureTimeArgument extends CommandArgument {

    private final HCF plugin;

    public EventSetCaptureTimeArgument(HCF plugin)
    {

        super("setdefaultcaptime", "Renames an event");

        this.plugin = plugin;

        this.permission = ("hcf.command.event.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <time>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }


        Faction faction = this.plugin.getFactionManager().getFaction(args[1]);

        if (!(faction instanceof KothFaction)) {

            sender.sendMessage(ConfigurationService.RED + "There is not a koth faction named '" + args[1] + "'.");

            return true;

        }

        KothFaction kothFaction = (KothFaction) faction;

        if (kothFaction.getCaptureZone() == null) {
            sender.sendMessage(ConfigurationService.RED + "No capture zone defined");
        } else {
            long time = JavaUtils.parse(args[2]);
            if (time <= 0) {
                sender.sendMessage(ConfigurationService.RED + "Invalid time");
            } else {
                CaptureZone captureZone = kothFaction.getCaptureZone();
                captureZone.setDefaultCaptureMillis(time);
                if (captureZone.getRemainingCaptureMillis() > time) {
                    captureZone.setRemainingCaptureMillis(time);
                }
                sender.sendMessage(ConfigurationService.YELLOW + "Set default capture time to " + DurationFormatUtils.formatDurationWords(time, true, true));
            }
        }

        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {

            return Collections.emptyList();

        }

        return (List) this.plugin.getFactionManager();

    }
}
