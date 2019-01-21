package com.faithfulmc.hardcorefactions.events.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventTimer;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;


public class EventCancelArgument extends CommandArgument {
    private final HCF plugin;


    public EventCancelArgument(HCF plugin) {
        super("cancel", "Cancels a running event", new String[]{"stop", "end"});
        this.plugin = plugin;
        this.permission = ("hcf.command.event.argument." + getName());
    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName();

    }


    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        EventTimer eventTimer = this.plugin.getTimerManager().eventTimer;
        EventFaction eventFaction = eventTimer.getEventFaction();
        if (!eventTimer.clearCooldown()) {
            if(!eventTimer.isNextCancelled()){
                eventTimer.setNextCancelled(true);
                sender.sendMessage(ConfigurationService.YELLOW + "Cancelled the upcoming scheduled event (" + (eventTimer.getNextEventFaction() != null ? eventTimer.getNextEventFaction().getName() : "None") + ")");
            }
            else{
                eventTimer.setNextCancelled(false);
                sender.sendMessage(ConfigurationService.YELLOW + "Rescheduled the upcoming event (" + (eventTimer.getNextEventFaction() != null ? eventTimer.getNextEventFaction().getName() : "None") + ")");
            }
            return true;
        }

        Bukkit.broadcastMessage(eventFaction.getEventType().getPrefix() + ConfigurationService.GOLD + sender.getName() + ConfigurationService.YELLOW + " has cancelled " + eventFaction.getName() + ConfigurationService.YELLOW + ".");
        return true;

    }

}