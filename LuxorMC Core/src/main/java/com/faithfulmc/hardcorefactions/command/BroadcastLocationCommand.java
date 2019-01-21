package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class BroadcastLocationCommand implements CommandExecutor{
    private static final String LOCATIONCOMMAND = "broadcastlocation", LOCATIONTOGGLE = "locationalerts";
    public static final String LOCATION_META = "LOCATION_ALERTS";

    private final HCF plugin;

    public BroadcastLocationCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(command.getName().equals(LOCATIONCOMMAND)) {
                Location location = player.getLocation();
                Faction faction = plugin.getFactionManager().getFactionAt(location);
                if (faction != null && faction.isSafezone()) {
                    player.sendMessage(ConfigurationService.RED + "You may not do this in a safezone");
                }else {
                    FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                    if(factionUser != null && factionUser.getFaction() != null && factionUser.getPlayerFaction().getMember(player) != null) {
                        factionUser.getPlayerFaction().broadcast(ChatColor.GREEN + "(Faction) " + player.getName() + ChatColor.GRAY + " is located at " + ChatColor.WHITE + "[" + location.getBlockX() + "]["+ location.getBlockZ() + "]");
                    }
                    else{
                        player.sendMessage(ConfigurationService.RED + "You must be in a faction to do this");
                    }
                }
            } else if (command.getName().equals(LOCATIONTOGGLE)) {
                FactionUser factionUser = plugin.getUserManager().getIfContains(player.getUniqueId());
                if(factionUser != null) {
                    factionUser.setLffalerts(!factionUser.isLffalerts());
                    sender.sendMessage(ConfigurationService.YELLOW + "Location alerts are now " + (factionUser.isLffalerts() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                    if(factionUser.isFdalerts()){
                        player.removeMetadata(LOCATION_META, plugin);
                    }
                    else{
                        player.setMetadata(LOCATION_META, new FixedMetadataValue(plugin, true));
                    }
                }
            }
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "You must be a player to do this");
        }
        return true;
    }
}
