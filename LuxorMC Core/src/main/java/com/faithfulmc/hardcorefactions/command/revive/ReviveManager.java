package com.faithfulmc.hardcorefactions.command.revive;

import com.faithfulmc.framework.command.SimpleCommandManager;
import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.deathban.Deathban;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.Config;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReviveManager implements Listener{
    private final HCF hcf;
    private CaseInsensitiveMap<String, ReviveCommand> commandMap = new CaseInsensitiveMap<>();

    public ReviveManager(HCF hcf) {
        this.hcf = hcf;
        Config config = new Config(hcf, "donatorrevive");
        for(String name: config.getKeys(false)){
            String permission = name + ".use";
            String prefix = ChatColor.translateAlternateColorCodes('&', config.getString(name + ".prefix"));
            String display = ChatColor.translateAlternateColorCodes('&', config.getString(name + ".name"));
            long cooldown = TimeUnit.MINUTES.toMillis(config.getInt(name + ".cooldown"));
            commandMap.put(name, new ReviveCommand(name, permission, prefix, display, cooldown));
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        String message = event.getMessage();
        if(message.startsWith("/")){
            message = message.substring(1);
            String[] fullArgs = message.split(" ");
            ReviveCommand command = commandMap.get(fullArgs[0]);
            if(command != null){
                event.setCancelled(true);
                if(player.hasPermission(command.getPermission())){
                    if(fullArgs.length != 3 || !fullArgs[1].equalsIgnoreCase("revive")){
                        player.sendMessage(command.getPrefix() + "/" + command.getName() + " revive <player>");
                    }
                    else{
                        UUID target = hcf.getUserManager().fetchUUID(fullArgs[2]);
                        FactionUser targetUser;
                        if(target == null || (targetUser = hcf.getUserManager().getUser(target)).getName() == null){
                            player.sendMessage(command.getPrefix() + "Player not found");
                        }
                        else if (hcf.getEotwHandler().isEndOfTheWorld()) {
                            player.sendMessage(command.getPrefix() + "You may not do this during EOTW");
                        }
                        else{
                            Deathban deathban = targetUser.getDeathban();
                            long now = System.currentTimeMillis();
                            if (deathban != null && deathban.getExpiryMillis() > now) {
                                FactionUser factionPlayer = hcf.getUserManager().getUser(player.getUniqueId());
                                long lastRevive = factionPlayer.getLastRevive();
                                long diff = now - lastRevive;
                                if (diff < command.getCooldown()) {
                                    long cool = command.getCooldown() - diff;
                                    player.sendMessage(command.getPrefix() + ConfigurationService.YELLOW + "You are on cooldown for " + ConfigurationService.GOLD + DurationFormatUtils.formatDurationWords(cool, true, true));
                                }
                                else{
                                    targetUser.removeDeathban();
                                    factionPlayer.setLastRevive(now);
                                    player.sendMessage(command.getPrefix() + "You removed the deathban of " + ConfigurationService.GOLD + targetUser.getName());
                                    Bukkit.broadcastMessage(ConfigurationService.GRAY + "[" + ConfigurationService.YELLOW + "*" + ConfigurationService.GRAY + "] " + ConfigurationService.GOLD + player.getName() + ConfigurationService.YELLOW + " used their " + command.getDisplay() + " Rank" + ConfigurationService.YELLOW + " to revive " + ConfigurationService.GOLD + targetUser.getName());
                                }
                            }
                            else{
                                player.sendMessage(command.getPrefix() + "That player is not deathbanned");
                            }
                        }
                    }
                }
                else{
                    player.sendMessage(SimpleCommandManager.PERMISSION_MESSAGE);
                }
            }
        }
    }

}
