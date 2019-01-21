package com.faithfulmc.hardcorefactions.listener.fixes;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HungerFixListener implements Listener {
    private final HCF hcf;

    public HungerFixListener(HCF hcf) {
        this.hcf = hcf;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if ((   from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ()) &&
                (event.getPlayer().getFoodLevel() < 20) &&
                (hcf.getFactionManager().getFactionAt(player.getLocation()).isSafezone())) {
            player.setFoodLevel(20);
            player.setSaturation(20.0F);
        }
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event) {
        if ((event.getEntity() instanceof Player)) {
            Player player = (Player) event.getEntity();
            if (hcf.getFactionManager().getFactionAt(player.getLocation()).isSafezone()) {
                player.setSaturation(20.0F);
                player.setHealth(20.0D);
            }
            player.setSaturation(10.0F);
        }
    }
}
