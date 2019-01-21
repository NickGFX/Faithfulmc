package com.faithfulmc.hardcorefactions.mountain;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.Config;
import com.faithfulmc.util.GenericUtils;
import com.faithfulmc.util.cuboid.Cuboid;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.selections.Selection;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OreMountainManager implements Runnable {
    private static final long DEFAULTTIME = TimeUnit.HOURS.toMillis(1);
    public final Set<Integer> ALLOWED = new HashSet<>(Arrays.asList(Material.COAL_ORE.getId(), Material.DIAMOND_ORE.getId(), Material.EMERALD_ORE.getId(), Material.GLOWING_REDSTONE_ORE.getId(), Material.GOLD_ORE.getId(), Material.IRON_ORE.getId(), Material.LAPIS_ORE.getId(), Material.REDSTONE_ORE.getId()));
    private final HCF hcf;
    private final File schematicfile = new File("plugins/WorldEdit/schematics/oremountain.schematic");
    private Config config;
    private Cuboid cuboid;
    private long time, lasttime;
    private int[] msgs = {45, 30, 15, 5, 1};
    private Set<Integer> messages = new HashSet<>();
    private CuboidClipboard cuboidclipboard = null;

    public OreMountainManager(HCF hcf) {
        this.hcf = hcf;
        config = new Config(hcf, "oremountain");
        cuboid = (Cuboid) config.get("cuboid");
        time = config.getLong("time", DEFAULTTIME);
        lasttime = config.getLong("lasttime", 0);
        Object messages = config.get("messages");
        if (messages != null) {
            this.messages.addAll(GenericUtils.createList(messages, Integer.class));
        }
        Bukkit.getScheduler().runTaskTimer(hcf, this, 20, 20);
        updateSelection();
        new BukkitRunnable() {
            public void run() {
                setCuboid(cuboid);
            }
        }.runTask(hcf);
    }

    public String createInformation() {
        if (cuboid == null) {
            return "None";
        }
        World world = cuboid.getWorld();
        String worldname = world.getEnvironment() == World.Environment.NETHER ? "Nether" : world.getEnvironment() == World.Environment.NORMAL ? "Overworld" : "End";
        String xz = cuboid.getCenter().getBlockX() + " | " + cuboid.getCenter().getBlockZ();
        return worldname + ", " + xz;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getLasttime() {
        return lasttime;
    }

    public void setLasttime(long lasttime) {
        this.lasttime = lasttime;
    }

    public HCF getHcf() {
        return hcf;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public void setCuboid(Cuboid cuboid) {
        this.cuboid = cuboid;
        OreFaction oreFaction = (OreFaction) hcf.getFactionManager().getFaction("OreFaction");
        oreFaction.reload();
    }

    public void constructCuboid(Selection selection) {
        cuboid = new Cuboid(selection.getMinimumPoint(), selection.getMaximumPoint());
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public boolean updateSelection() {
        if (!schematicfile.exists()) {
            System.out.println("No ore mountain schematic defined");
        } else if (cuboid == null) {
            System.out.println("No cuboid defined");
        } else {
            try {
                cuboidclipboard = CuboidClipboard.loadSchematic(schematicfile);
                cuboidclipboard.setOffset(new Vector());
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void setBlocks() {
        if (cuboidclipboard == null || cuboid == null || cuboid.getWorld() == null) {
            System.out.println("No ore mountain cuboid defined");
        } else {
            try {
                com.sk89q.worldedit.world.World w = null;
                for (com.sk89q.worldedit.world.World world : hcf.getWorldEdit().getWorldEdit().getServer().getWorlds()) {
                    if (world.getName().equals(cuboid.getWorld().getName())) {
                        w = world;
                        break;
                    }
                }
                if (w == null) {
                    System.out.println("No ore mountain schematic world defined");
                } else {
                    EditSession editSession = hcf.getWorldEdit().getWorldEdit().getEditSessionFactory().getEditSession(w, -1);
                    Vector size = cuboidclipboard.getSize();
                    for (int x = 0; x < size.getBlockX(); ++x) {
                        for (int y = 0; y < size.getBlockY(); ++y) {
                            for (int z = 0; z < size.getBlockZ(); ++z) {
                                Vector vector = new Vector(x, y, z);
                                BaseBlock block = cuboidclipboard.getBlock(vector);
                                if (block != null && ALLOWED.contains(block.getType())) {
                                    editSession.setBlock(vector.add(cuboidclipboard.getOrigin()), block);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void run() {
        if (cuboid != null) {
            long now = System.currentTimeMillis();
            long diff = now - lasttime;
            if (diff > time) {
                setBlocks();
                lasttime = now;
                Bukkit.broadcastMessage(ChatColor.GRAY + "The " + ChatColor.BLUE + ChatColor.BOLD + "Ore Mountain" + ChatColor.GRAY + " has reset");
                messages.clear();
            } else {
                for (int time : msgs) {
                    long duration = this.time - TimeUnit.MINUTES.toMillis(time);
                    if (diff > duration && messages.add(time)) {
                        Bukkit.broadcastMessage(ChatColor.GRAY + "The " + ChatColor.BLUE + ChatColor.BOLD + "Ore Mountain" + ChatColor.GRAY + " is resetting in " + ChatColor.BLUE + DurationFormatUtils.formatDurationWords(TimeUnit.MINUTES.toMillis(time), true, true));
                        break;
                    }
                }
            }
        }
    }

    public void save() {
        config.set("time", time);
        config.set("cuboid", cuboid);
        config.set("lasttime", lasttime);
        config.set("messages", new LinkedList<>(messages));
        config.save();
    }
}
