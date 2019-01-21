package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.FactionManager;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.google.common.base.Joiner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class NearCommand implements CommandExecutor {
    public static final int RADIUS = 30;
    
    private final HCF hcf;

    public NearCommand(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            List<Player> playerList = getNearbyEnemies(player);
            if(playerList.isEmpty()){
                sender.sendMessage(ConfigurationService.YELLOW + "There are no visible enemies in a " + RADIUS + " block radius");
            }
            else{
                sender.sendMessage(ConfigurationService.YELLOW + "Nearby visible enemies in a " + RADIUS + " block radius");
                sender.sendMessage(ConfigurationService.GRAY + Joiner.on(", ").join(playerList.stream().map(Player::getName).collect(Collectors.toList())));
            }
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "You need to be a player to do this");
        }
        return true;
    }

    public List<Player> getNearbyEnemies(Player player) {
        List<Player> players = new ArrayList<>();
        FactionManager factionManager = this.hcf.getFactionManager();
        Faction playerFaction = factionManager.getPlayerFaction(player.getUniqueId());
        Collection<Entity> nearby = player.getNearbyEntities((double) RADIUS, (double) RADIUS, (double) RADIUS);
        for (final Entity entity : nearby) {
            if (entity instanceof Player) {
                final Player target = (Player) entity;
                if (!target.canSee(player)) {
                    continue;
                }
                if (!player.canSee(target)) {
                    continue;
                }
                if(target.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                    continue;
                }
                final Faction targetFaction;
                if (playerFaction != null && (targetFaction = factionManager.getPlayerFaction(target)) != null && targetFaction.equals(playerFaction)) {
                    continue;
                }
                players.add(target);
            }
        }
        return players;
    }
}
