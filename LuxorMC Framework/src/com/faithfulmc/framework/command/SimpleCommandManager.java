package com.faithfulmc.framework.command;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SimpleCommandManager implements CommandManager {
    public static final String PERMISSION_MESSAGE;

    static {
        PERMISSION_MESSAGE = BaseConstants.GOLD + ChatColor.BOLD.toString() + BaseConstants.NAME + "MC " + ChatColor.DARK_GRAY + "»" + ChatColor.RED + " You do not have permission to execute this command.";
    }

    private final Map<String, BaseCommand> commandMap;

    public SimpleCommandManager(final BasePlugin plugin) {
        this.commandMap = new HashMap();
        final ConsoleCommandSender console = plugin.getServer().getConsoleSender();
        new BukkitRunnable() {
            public void run() {
                final Collection<BaseCommand> commands = SimpleCommandManager.this.commandMap.values();
                for (final BaseCommand command : commands) {
                    final String commandName = command.getName();
                    final PluginCommand pluginCommand = plugin.getCommand(commandName);
                    if (pluginCommand == null) {
                        Bukkit.broadcastMessage(commandName);
                        console.sendMessage('[' + plugin.getName() + "] " + BaseConstants.YELLOW + "Failed to register command '" + commandName + "'.");
                        console.sendMessage('[' + plugin.getName() + "] " + BaseConstants.YELLOW + "Reason: Undefined in plugin.yml.");
                    } else {
                        pluginCommand.setAliases((List<String>) Arrays.asList(command.getAliases()));
                        pluginCommand.setDescription(command.getDescription());
                        pluginCommand.setExecutor((CommandExecutor) command);
                        pluginCommand.setTabCompleter((TabCompleter) command);
                        pluginCommand.setUsage(command.getUsage());
                        pluginCommand.setPermission("base.command." + command.getName());
                        pluginCommand.setPermissionMessage(SimpleCommandManager.PERMISSION_MESSAGE);
                    }
                }
            }
        }.runTask((Plugin) plugin);
    }

    @Override
    public boolean containsCommand(final BaseCommand command) {
        return this.commandMap.containsValue(command);
    }

    @Override
    public void registerAll(final BaseCommandModule module) {
        if (module.isEnabled()) {
            final Set<BaseCommand> commands = module.getCommands();
            for (final BaseCommand command : commands) {
                this.commandMap.put(command.getName(), command);
            }
        }
    }

    @Override
    public void registerCommand(final BaseCommand command) {
        this.commandMap.put(command.getName(), command);
    }

    @Override
    public void registerCommands(final BaseCommand[] commands) {
        for (final BaseCommand command : commands) {
            this.commandMap.put(command.getName(), command);
        }
    }

    @Override
    public void unregisterCommand(final BaseCommand command) {
        this.commandMap.values().remove(command);
    }

    @Override
    public BaseCommand getCommand(final String id) {
        return this.commandMap.get(id);
    }
}
