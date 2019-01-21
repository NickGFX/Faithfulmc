package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class ToggleDeathMessagesCommand implements CommandExecutor, Listener{
    public static final String NO_DEATH_MESSAGES_META = "NO_DEATH_MESSAGES";

    private final HCF plugin;

    public ToggleDeathMessagesCommand(HCF plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event){
        String deathMessage = event.getDeathMessage();
        String stripped = ChatColor.stripColor(deathMessage);
        plugin.getLogger().info("[DEATH] " + stripped);
        for(Player player: Bukkit.getOnlinePlayers()){
            if(!player.hasMetadata(NO_DEATH_MESSAGES_META)){
                player.sendMessage(deathMessage);
            }
        }
        event.setDeathMessage(null);
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;
            FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
            if(factionUser != null){
                factionUser.setDeathMessages(!factionUser.isDeathMessages());
                if(!factionUser.isDeathMessages()){
                    player.setMetadata(NO_DEATH_MESSAGES_META, new FixedMetadataValue(plugin, true));
                }
                else{
                    player.removeMetadata(NO_DEATH_MESSAGES_META, plugin);
                }
                player.sendMessage(ConfigurationService.YELLOW + "You have " + (factionUser.isDeathMessages() ? ConfigurationService.GREEN + "enabled" : ConfigurationService.RED + "disabled") + ConfigurationService.YELLOW + " death messages.");
            }
        }
        else{
            commandSender.sendMessage(ConfigurationService.RED + "You must be a player to do this.");
        }
        return true;
    }
}
