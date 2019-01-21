package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DisguiseCommand implements CommandExecutor{
    private final HCF plugin;
    private final EntityType[] ALLOWED_TYPES = { EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER, EntityType.PIG, EntityType.SHEEP, EntityType.COW, EntityType.ENDERMAN};
    private final long COOLDOWN = TimeUnit.MINUTES.toMillis(2);


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(args.length == 0){
                if(((CraftPlayer) player).getHandle().isDisguised()){
                    player.sendMessage(ChatColor.YELLOW + "You are currently disguised as a " + ChatColor.GRAY + ((CraftPlayer) player).getHandle().getDisguiseType().name().toLowerCase().replace("_", " ") + ChatColor.YELLOW + ", to undisguise use /" + command.getName() + " none");
                    return true;
                }
                sender.sendMessage(ChatColor.YELLOW + "Please select an entity type: " + ChatColor.GRAY + Arrays.stream(ALLOWED_TYPES).map(EntityType::name).map(String::toLowerCase).map(s -> s.replace("_", " ")).collect(Collectors.joining(", ")));
            }
            else{
                String typeString = Arrays.stream(args).collect(Collectors.joining(" ")).toLowerCase();
                if(typeString.equals("none") || typeString.equals("disable") || typeString.equals("off")){
                    if(((CraftPlayer) player).getHandle().isDisguised()){
                        ((CraftPlayer) player).getHandle().disguise(null);
                        player.sendMessage(ChatColor.YELLOW + "You are no longer disguised");
                    } else{
                        player.sendMessage(ChatColor.YELLOW + "You are not disguised");
                    }
                    return true;
                }
                if(plugin.getTimerManager().spawnTagTimer.getRemaining(player) > 0){
                    player.sendMessage(ChatColor.YELLOW + "You may not disguise whilst in combat.");
                    return true;
                }
                FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                long lastDisguise = factionUser.getLastDisguise();
                long now = System.currentTimeMillis();
                if(now - lastDisguise > COOLDOWN || player.hasPermission("hcf.command.disguise.bypasscooldown")) {
                    EntityType selected = null;
                    for (EntityType entityType : ALLOWED_TYPES) {
                        if (entityType.name().toLowerCase().replace("_", " ").equals(typeString)) {
                            selected = entityType;
                        }
                    }
                    if (selected == null) {
                        sender.sendMessage(ChatColor.YELLOW + "Please select an entity type: " + ChatColor.GRAY + Arrays.stream(ALLOWED_TYPES).map(EntityType::name).map(String::toLowerCase).map(s -> s.replace("_", " ")).collect(Collectors.joining(", ")));
                    } else {
                        factionUser.setLastDisguise(now);
                        ((CraftPlayer) player).getHandle().disguise(selected);
                        sender.sendMessage(ChatColor.YELLOW + "Successfully disguised as a " + ChatColor.GRAY + selected.name().toLowerCase().replace("_", " "));
                    }
                }
                else{
                    sender.sendMessage(ChatColor.YELLOW + "You may not use this for another " + ChatColor.GRAY + DurationFormatUtils.formatDurationWords(COOLDOWN - (now - lastDisguise), true, true));
                }
            }
        }
        return true;
    }
}
