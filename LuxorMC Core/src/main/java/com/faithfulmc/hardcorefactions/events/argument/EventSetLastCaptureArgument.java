package com.faithfulmc.hardcorefactions.events.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.CitadelCapture;
import com.faithfulmc.hardcorefactions.events.faction.CitadelFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class EventSetLastCaptureArgument extends CommandArgument{
    private final HCF plugin;


    public EventSetLastCaptureArgument(HCF plugin) {

        super("setlastcappers", "Sets the last capturers of a citadel even");

        this.plugin = plugin;

        this.aliases = new String[]{"setlastcapure"};

        this.permission = ("hcf.command.event.argument." + getName());

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <citadelName> <factionName/NONE>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 3) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }

        Faction faction = this.plugin.getFactionManager().getFaction(args[1]);

        if (!(faction instanceof CitadelFaction)) {

            sender.sendMessage(ConfigurationService.RED + "There is not a citadel faction named '" + args[1] + "'.");

            return true;

        }

        CitadelFaction citadelFaction = (CitadelFaction) faction;
        CitadelCapture citadelCapture = citadelFaction.getCitadelCapture();

        if(args[2].equalsIgnoreCase("NONE")) {
            if(citadelCapture != null) {
                citadelFaction.setCitadelCapture(null);
                sender.sendMessage(ConfigurationService.YELLOW + "Removed cappers of citadel");
            }
            else{
                sender.sendMessage(ConfigurationService.RED + "There is currently no citadel cappers");
            }
        }
        else {
            Faction targetFaction = this.plugin.getFactionManager().getFaction(args[2]);
            if (targetFaction != null && targetFaction instanceof PlayerFaction) {
                if(citadelCapture == null){
                    citadelCapture = new CitadelCapture(targetFaction, System.currentTimeMillis());
                    citadelFaction.setCitadelCapture(citadelCapture);
                }
                else{
                    citadelCapture.setFaction(targetFaction);
                    citadelCapture.setFactionUUID(targetFaction.getUniqueID());
                    citadelCapture.setLoaded(true);
                }
                sender.sendMessage(ConfigurationService.YELLOW + "Set citadel cappers to " + targetFaction.getDisplayName(sender));
            }
            else{
                sender.sendMessage(ConfigurationService.RED + "No faction found");
            }
        }
        return true;

    }


    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {

            return Collections.emptyList();

        }

        return Collections.emptyList();

    }
}
