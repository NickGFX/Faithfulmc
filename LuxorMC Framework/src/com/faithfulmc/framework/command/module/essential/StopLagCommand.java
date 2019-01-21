package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class StopLagCommand extends BaseCommand {
    private final BasePlugin plugin;

    public StopLagCommand(final BasePlugin plugin) {
        super("stoplag", "Decrease the server lag.");
        this.plugin = plugin;
        this.setUsage("/(command)");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final boolean newMode = !this.plugin.getServerHandler().isDecreasedLagMode();
        this.plugin.getServerHandler().setDecreasedLagMode(newMode);
        final String newModeString = Boolean.toString(newMode);
        for (final World world : Bukkit.getWorlds()) {
            world.setGameRuleValue("doDaylightCycle", newModeString);
        }
        Command.broadcastCommandMessage(sender, ChatColor.LIGHT_PURPLE + "Server is " + (newMode ? (ChatColor.RED + "no longer") : (ChatColor.GREEN + "now")) + ChatColor.LIGHT_PURPLE + " allowing intensive activity." + (newMode ? "" : " Blocks won't burn, have physics, form, spread, travel, or be ignited. Natural entities won't spawn or explode. Daylight cycle game-rule will be disabled."));
        return true;
    }
}
