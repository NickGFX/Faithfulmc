package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.BukkitUtils;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LFFCommand implements CommandExecutor {
    private final HCF plugin;

    private static final TimeUnit UNIT = TimeUnit.HOURS;
    private static final int TIME = 1;
    private static final long MILLIS = UNIT.toMillis(TIME);
    private static final String WORDS = DurationFormatUtils.formatDurationWords(MILLIS, true, true);

    private final Map timestampMap = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(TIME, UNIT).build().asMap();

    public LFFCommand(HCF plugin) {
        this.plugin = plugin;
    }

    private static final String LFFCOMMAND = "lff", LFFTOGGLE = "lffalerts";
    public static final String LFF_META = "LFF_NOALERTS";

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equals(LFFCOMMAND)) {
                Faction faction = plugin.getFactionManager().getPlayerFaction(player);
                if(faction == null) {
                    Long timeStamp = (Long) timestampMap.get(player.getUniqueId());
                    long now = System.currentTimeMillis();
                    long diff;
                    if (timeStamp == null || (diff = now - timeStamp) > MILLIS) {
                        String bar = ConfigurationService.ARROW_COLOR + BukkitUtils.STRAIGHT_LINE_DEFAULT.substring(0, 40);
                        String message = " " + ConfigurationService.ARROW_COLOR + ConfigurationService.DOUBLEARROW + " " + ConfigurationService.GREEN + ChatColor.BOLD.toString() + player.getName() + ConfigurationService.YELLOW + " is looking to join a " + (ConfigurationService.LUXOR ? ChatColor.AQUA : ChatColor.RED) + ChatColor.BOLD + "faction";
                        for (Player other : Bukkit.getOnlinePlayers()) {
                            if (!other.hasMetadata(LFF_META)) {
                                other.sendMessage(bar);
                                other.sendMessage(message);
                                other.sendMessage(bar);
                            }
                        }
                        timestampMap.put(player.getUniqueId(), now);
                        player.sendMessage(ConfigurationService.YELLOW + "You have announced that you are looking for a faction, you must wait " + WORDS + " before doing this again.");
                    } else {
                        player.sendMessage(ConfigurationService.RED + "You may not use this for " + ChatColor.BOLD + DurationFormatUtils.formatDurationWords(MILLIS - diff, true, true));
                    }
                }
                else{
                    player.sendMessage(ChatColor.RED + "You are already in a faction");
                }
            } else if (cmd.getName().equals(LFFTOGGLE)) {
                FactionUser factionUser = plugin.getUserManager().getIfContains(player.getUniqueId());
                if(factionUser != null) {
                    factionUser.setLffalerts(!factionUser.isLffalerts());
                    sender.sendMessage(ConfigurationService.YELLOW + "LFF alerts are now " + (factionUser.isLffalerts() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
                    if(factionUser.isFdalerts()){
                        player.removeMetadata(LFF_META, plugin);
                    }
                    else{
                        player.setMetadata(LFF_META, new FixedMetadataValue(plugin, true));
                    }
                }
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "You must be a player to do this");
        }
        return false;
    }
}

