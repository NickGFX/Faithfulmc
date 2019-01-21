package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.text.DecimalFormat;
import java.util.List;

public class LagCommand extends BaseCommand{
    private static final long MEGABYTE = 1048576L;
    private static final long GIGABYTE = 1024 * MEGABYTE;
    private static final double MAXIMUM_TPS = 20.0;

    private long lastServerTick = 0;

    public LagCommand() {
        super("lag", "Checks the lag of the server.");
        this.setUsage("/(command)");
    }
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        double tps = Bukkit.spigot().getTPS()[0];
        double lag = Math.round((1.0 - tps / MAXIMUM_TPS) * 100.0);
        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        sender.sendMessage(BaseConstants.GOLD + "TPS: " + BaseConstants.YELLOW + decimalFormat.format(tps) + " " + BaseConstants.GOLD + "Lag: " + BaseConstants.YELLOW + decimalFormat.format(lag) + "%");
        if(Bukkit.spigot().getTPS().length == 4){
            sender.sendMessage(BaseConstants.GOLD + "Full Server Tick: " + BaseConstants.YELLOW + decimalFormat.format(Bukkit.spigot().getTPS()[3]) + "ms");
        }
        if (sender.hasPermission(command.getPermission() + ".memory")) {
            final Runtime runtime = Runtime.getRuntime();
            sender.sendMessage(BaseConstants.GOLD + "Available Processors: " + BaseConstants.YELLOW+ runtime.availableProcessors());
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long currentMemory = totalMemory - freeMemory;
            double percentage = ((double) currentMemory / (double) totalMemory) * 100;
            sender.sendMessage(BaseConstants.GOLD + "Maximum Memory: " + BaseConstants.YELLOW + decimalFormat.format((double) maxMemory / (double) GIGABYTE) + "GB");
            sender.sendMessage(BaseConstants.GOLD + "Memory Usage: " + BaseConstants.YELLOW + decimalFormat.format((double) currentMemory / (double) GIGABYTE) + "GB / " + decimalFormat.format((double) totalMemory / (double) GIGABYTE) + "GB");
            sender.sendMessage(BaseConstants.GOLD + "Percentage Usage: " + BaseConstants.YELLOW + decimalFormat.format(percentage) + "%");
            final List<World> worlds = Bukkit.getWorlds();
            for (final World world : worlds) {
                final World.Environment environment = world.getEnvironment();
                final String environmentName = WordUtils.capitalizeFully(environment.name().replace('_', ' '));
                int tileEntities = 0;
                final Chunk[] loadedChunks3;
                final Chunk[] var13;
                final Chunk[] loadedChunks2 = var13 = (loadedChunks3 = world.getLoadedChunks());
                for (int var14 = loadedChunks2.length, var15 = 0; var15 < var14; ++var15) {
                    final Chunk chunk = var13[var15];
                    tileEntities += chunk.getTileEntities().length;
                }
                sender.sendMessage(ChatColor.RED + world.getName() + '(' + environmentName + "): " + BaseConstants.YELLOW + loadedChunks3.length + " chunks, " + world.getEntities().size() + " entities, " + tileEntities + " tile entities.");
            }
        }
        return true;
    }
}
