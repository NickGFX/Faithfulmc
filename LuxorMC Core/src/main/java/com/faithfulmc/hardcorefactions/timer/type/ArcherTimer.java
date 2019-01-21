package com.faithfulmc.hardcorefactions.timer.type;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.hcfclass.archer.ArcherClass;
import com.faithfulmc.hardcorefactions.scoreboard.PlayerBoard;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.hardcorefactions.timer.event.TimerExpireEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ArcherTimer extends PlayerTimer implements org.bukkit.event.Listener {
    private final HCF plugin;


    public ArcherTimer(HCF plugin) {
        super("Archer Tag", TimeUnit.SECONDS.toMillis(15L), false);
        this.plugin = plugin;
    }


    public String getScoreboardPrefix() {
        return ConfigurationService.RED.toString() + ChatColor.BOLD;
    }


    public void run() {
    }


    @EventHandler
    public void onExpire(TimerExpireEvent e) {
        if ((e.getUserUUID().isPresent()) && (e.getTimer().equals(this))) {
            UUID userUUID = e.getUserUUID().get();
            Player player = Bukkit.getPlayer(userUUID);
            if (player == null) {
                return;
            }
            Player p = Bukkit.getPlayer(ArcherClass.TAGGED.get(userUUID));
            if (p != null) {
                p.sendMessage(ConfigurationService.YELLOW + "Your archer tag on " + ConfigurationService.GOLD + player.getName() + ConfigurationService.YELLOW + " has expired.");
            }
            player.sendMessage(ConfigurationService.YELLOW + "You're no longer archer tagged.");
            ArcherClass.TAGGED.remove(player.getUniqueId());
            for(PlayerBoard playerBoard: plugin.getScoreboardHandler().getPlayerBoards().values()){
                playerBoard.init(player);
            }
        }
    }

    public static final double MULTIPLIER = ConfigurationService.ORIGINS ? 0.5 : 0.3;

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof Player) {
            Player attacked = (Player) entity;
            if(getRemaining(attacked) > 0) {
                Entity damagerEntity = event.getDamager();
                if (damagerEntity instanceof Player) {
                    double damage = event.getDamage() * MULTIPLIER;
                    event.setDamage(event.getDamage() + damage);
                } else if (damagerEntity instanceof Arrow) {
                    Projectile arrow = (Projectile) damagerEntity;
                    if (arrow.getShooter() instanceof Player) {
                        Player damager = (Player) arrow.getShooter();
                        if (ArcherClass.TAGGED.get(attacked.getUniqueId()) == damager.getUniqueId()) {
                            setCooldown(attacked, attacked.getUniqueId());
                        }
                        double damage = event.getDamage() * MULTIPLIER;
                        event.setDamage(event.getDamage() + damage);
                    }
                }
            }
        }
    }
}