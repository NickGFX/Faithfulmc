package com.faithfulmc.hardcorefactions.events;


import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.argument.*;
import com.faithfulmc.util.command.ArgumentExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class EventExecutor extends ArgumentExecutor {
    private EventScheduleArgument eventScheduleArgument;

    public EventExecutor(HCF plugin) {
        super("event");
        addArgument(new EventCreateArgument(plugin));
        addArgument(eventScheduleArgument = new EventScheduleArgument(plugin));
        addArgument(new EventCancelArgument(plugin));
        addArgument(new EventDeleteArgument(plugin));
        addArgument(new EventRenameArgument(plugin));
        addArgument(new EventSetAreaArgument(plugin));
        addArgument(new EventSetCapzoneArgument(plugin));
        addArgument(new EventSetCaptureTimeArgument(plugin));
        addArgument(new EventStartArgument(plugin));
        addArgument(new EventUptimeArgument(plugin));
        addArgument(new EventRegenArgument(plugin));
        addArgument(new EventSetLastCaptureArgument(plugin));
        addArgument(new EventResetChestTimeArgument(plugin));
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission(command.getPermission() + ".arguments")){
            return eventScheduleArgument.onCommand(sender, command, label, args);
        }
        return super.onCommand(sender, command, label, args);
    }
}