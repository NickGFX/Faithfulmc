package com.faithfulmc.hardcorefactions.listener;

import com.faithfulmc.anticheat.util.MathHelper;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import net.minecraft.server.v1_7_R4.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class ElevatorListener implements Listener {
    private final HCF hcf;
    private String  prefix = ConfigurationService.YELLOW + "[Elevators] " + ChatColor.WHITE;
    private String signTitle = ChatColor.DARK_RED + "[Elevator]";

    public ElevatorListener(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignUpdate(SignChangeEvent event) {
        if (StringUtils.containsIgnoreCase(event.getLine(0), "Elevator")) {
            boolean up;
            if (StringUtils.containsIgnoreCase(event.getLine(1), "Up")) {
                up = true;
            } else if (StringUtils.containsIgnoreCase(event.getLine(1), "Down")) {
                up = false;
            } else {
                event.getPlayer().sendMessage(prefix + "Invalid sign! Needs to be Up or Down");
                fail(event);
                return;
            }
            event.setLine(0, signTitle);
            event.setLine(1, up ? "Up" : "Down");
            event.setLine(2, "");
            event.setLine(3, "");
        }
    }

    public void fail(SignChangeEvent e) {
        e.setLine(0, signTitle);
        e.setLine(1, ConfigurationService.RED + "Error");
        e.setLine(2, "");
        e.setLine(3, "");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                String[] lines = sign.getLines();
                if (lines[0].equals(signTitle)) {
                    boolean up;
                    if (lines[1].equalsIgnoreCase("Up")) {
                        up = true;
                    } else if (lines[1].equalsIgnoreCase("Down")) {
                        up = false;
                    } else {
                        return;
                    }
                    if (event.useInteractedBlock() == Event.Result.ALLOW) {
                        Player player = event.getPlayer();
                        if (!ProtectionListener.attemptBuild(player, block.getLocation(), null, true, false)) {
                            Location playerLocation = player.getLocation();
                            Location blockLocation = block.getLocation().add(0.5, 1, 0.5);
                            if(playerLocation.distanceSquared(blockLocation) > 20.25){
                                return;
                            }
                            Block rayTraceBlock = rayTrace(player);
                            if(rayTraceBlock == null || rayTraceBlock.getX() != block.getX() || rayTraceBlock.getY() != block.getY() || rayTraceBlock.getZ() != block.getZ()){
                                return;
                            }
                        }
                        signClick(player, sign.getLocation(), up);
                    }
                }
            }
        }
    }

    public Block rayTrace(Player bukkitPlayer){
        EntityPlayer player = ((CraftPlayer) bukkitPlayer).getHandle();
        float f = 1.0F;
        float f1 = player.lastPitch + (player.pitch - player.lastPitch) * f;
        float f2 = player.lastYaw + (player.yaw - player.lastYaw) * f;
        double d0 = player.lastX + (player.locX - player.lastX) * (double)f;
        double d1 = player.lastY + (player.locY - player.lastY) * (double)f + 1.62D - (double)player.height;
        double d2 = player.lastZ + (player.locZ - player.lastZ) * (double)f;
        Vec3D vec3d = Vec3D.a(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = player.playerInteractManager.getGameMode() == EnumGamemode.CREATIVE?5.0D:4.5D;
        Vec3D vec3d1 = vec3d.add((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
        MovingObjectPosition movingobjectposition = player.world.rayTrace(vec3d, vec3d1, false);
        if(movingobjectposition == null){
            return null;
        }
        else if(movingobjectposition.type != EnumMovingObjectType.BLOCK){
            return null;
        }
        else{
            return bukkitPlayer.getWorld().getBlockAt(movingobjectposition.b, movingobjectposition.c, movingobjectposition.d);
        }
    }

    public boolean signClick(Player player, Location signLocation, boolean up) {
        Block block = signLocation.getBlock();
        do {
            block = block.getRelative(up ? BlockFace.UP : BlockFace.DOWN);
            if (block.getY() > block.getWorld().getMaxHeight() || block.getY() <= 0) {
                player.sendMessage(prefix + "Could not locate the sign " + (up ? "above" : "below"));
                return false;
            }
        } while (!isSign(block));

        boolean underSafe = isSafe(block.getRelative(BlockFace.DOWN));
        boolean overSafe = isSafe(block.getRelative(BlockFace.UP));
        if (!underSafe && !overSafe) {
            player.sendMessage(prefix + "Could not find a place to teleport by the sign " + (up ? "above" : "below"));
            return false;
        }
        Location location = player.getLocation().clone();
        location.setX(block.getX() + 0.5);
        location.setY(block.getY() + (underSafe ? -1 : 0));
        location.setZ(block.getZ() + 0.5);
        location.setPitch(0);
        player.setMetadata("signClick", new FixedMetadataValue(HCF.getInstance(), true));
        player.teleport(location);
        return true;
    }

    public boolean isSign(Block block) {
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            String[] lines = sign.getLines();
            return lines[0].equals(signTitle) && (lines[1].equalsIgnoreCase("Up") || lines[1].equalsIgnoreCase("Down"));
        }
        return false;
    }

    public boolean isSafe(Block block) {
        if(block == null){
            return false;
        }
        Material type = block.getType();
        if(type == Material.AIR || type == Material.SIGN || type == Material.WALL_SIGN || type == Material.SIGN_POST){
            return true;
        }
        if(type.isBlock() && type.isSolid() && type != Material.GLASS && type != Material.STAINED_GLASS){
            return false;
        }
        return true;
    }
}
