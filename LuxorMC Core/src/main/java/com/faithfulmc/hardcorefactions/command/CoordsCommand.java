package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.CaptureZone;
import com.faithfulmc.hardcorefactions.events.faction.ConquestFaction;
import com.faithfulmc.hardcorefactions.events.faction.KothFaction;
import com.faithfulmc.hardcorefactions.faction.type.ClaimableFaction;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.mountain.GlowstoneMountainManager;
import com.faithfulmc.hardcorefactions.mountain.OreMountainManager;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.cuboid.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CoordsCommand implements CommandExecutor {
    private final HCF hcf;

    private long lastCache = 0;
    private final List<KothFaction> koths = new ArrayList<>();
    private final List<ConquestFaction> conquestFaction = new ArrayList<>();

    private ChatColor MAIN_COLOR;
    private ChatColor SECONDARY_COLOR;
    private ChatColor EXTRA_COLOR;
    private ChatColor VALUE_COLOR;
    private String STAR;

    public CoordsCommand(HCF hcf) {
        this.hcf = hcf;
        this.MAIN_COLOR = ConfigurationService.GOLD;
        this.SECONDARY_COLOR = ConfigurationService.YELLOW;
        this.EXTRA_COLOR = ChatColor.DARK_GRAY;
        this.VALUE_COLOR = ConfigurationService.GRAY;
        STAR = ConfigurationService.GOLD + " * ";
    }

    public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
        tryCache();
        sender.sendMessage(EXTRA_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ConfigurationService.SCOREBOARD_TITLE);
        sender.sendMessage(EXTRA_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        if (!ConfigurationService.KIT_MAP) {
            GlowstoneMountainManager glowstone = hcf.getGlowstoneMountainManager();
            OreMountainManager ores = hcf.getOreMountainManager();
            Cuboid gscuboid = glowstone.getCuboid();
            Cuboid orecuboid = ores.getCuboid();
            if (gscuboid != null || orecuboid != null) {
                sender.sendMessage(MAIN_COLOR + ChatColor.BOLD.toString() + "Mountains");
                if (gscuboid != null) {
                    sender.sendMessage(STAR + SECONDARY_COLOR + "Glowstone Mountain: " + VALUE_COLOR + GlowstoneMountainCommand.locToCords(gscuboid.getCenter()));
                }
                if (orecuboid != null) {
                    sender.sendMessage(STAR + SECONDARY_COLOR + "Ore Mountain: " + VALUE_COLOR + OreMountainCommand.locToCords(orecuboid.getCenter()));
                }
            }
        }
        if (!koths.isEmpty()) {
            sender.sendMessage(MAIN_COLOR + ChatColor.BOLD.toString() + "Koth" + (koths.size() == 1 ? "" : "s") + ":");
            for (KothFaction kothFaction : koths) {
                CaptureZone captureZone = kothFaction.getCaptureZone();
                if (captureZone == null) {
                    continue;
                }
                Cuboid cuboid = captureZone.getCuboid();
                if(cuboid == null){
                    continue;
                }
                World world = cuboid.getWorld();
                int x = (int) (Math.round(cuboid.getCenter().getX() / 10.0) * 10);
                int z = (int) (Math.round(cuboid.getCenter().getZ() / 10.0) * 10);
                sender.sendMessage(STAR + SECONDARY_COLOR + kothFaction.getName() + ":  " + VALUE_COLOR + ClaimableFaction.ENVIRONMENT_MAPPINGS.get(world.getEnvironment()) + ", " + x + " | " + z);

            }
        }
        if (!conquestFaction.isEmpty()) {
            sender.sendMessage(MAIN_COLOR + ChatColor.BOLD.toString() + "Conquest" + (conquestFaction.size() == 1 ? "" : "s") + ":");
            for (ConquestFaction conquest : conquestFaction) {
                CaptureZone captureZone = conquest.getMain();
                if (captureZone == null) {
                    continue;
                }
                Cuboid cuboid = captureZone.getCuboid();
                World world = cuboid.getWorld();
                int x = (int) (Math.round(cuboid.getCenter().getX() / 10.0) * 10);
                int z = (int) (Math.round(cuboid.getCenter().getZ() / 10.0) * 10);
                sender.sendMessage(STAR + SECONDARY_COLOR + conquest.getName() + ":  " + VALUE_COLOR + ClaimableFaction.ENVIRONMENT_MAPPINGS.get(world.getEnvironment()) + ", " + x + " | " + z);
            }
        }
        sender.sendMessage(EXTRA_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        return true;
    }

    public void tryCache(){
        long now = System.currentTimeMillis();
        if(now - lastCache > TimeUnit.HOURS.toMillis(1)) {
            koths.clear();
            conquestFaction.clear();
            for (Faction faction : new ArrayList<>(hcf.getFactionManager().getFactions())) {
                if (faction instanceof ClaimableFaction && ((ClaimableFaction) faction).getClaims().isEmpty()) {
                    continue;
                }
                if (faction instanceof KothFaction) {
                    koths.add((KothFaction) faction);
                }
                if (faction instanceof ConquestFaction) {
                    conquestFaction.add((ConquestFaction) faction);
                }
            }
            lastCache = now;
        }
    }
}
