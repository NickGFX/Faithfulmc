package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.EventCapture;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import com.faithfulmc.util.BukkitUtils;
import com.faithfulmc.util.chat.ClickAction;
import com.faithfulmc.util.chat.Text;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StatsCommand implements CommandExecutor {
    private final HCF plugin;

    public StatsCommand(HCF plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command cmd, String s, String[] args) {
        FactionUser factionUser;
        if (args.length == 0) {
            if(commandSender instanceof Player) {
                Player player = (Player) commandSender;
                factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                sendInformation(commandSender, factionUser);
            }
            else{
                commandSender.sendMessage(ConfigurationService.RED + "Invalid usage, /stats <player>");
            }
        }
        else{
            UUID uuid = plugin.getUserManager().fetchUUID(args[0]);
            if(uuid == null || (factionUser = plugin.getUserManager().getUser(uuid)).getName() == null){
                commandSender.sendMessage(ConfigurationService.RED + "Player not found");
            }
            else{
                sendInformation(commandSender, factionUser);
            }
        }
        return true;
    }

    public void sendInformation(CommandSender player, FactionUser target) {
        player.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        player.sendMessage(ConfigurationService.GOLD + ChatColor.BOLD.toString() + "Stats of " + target.getName());
        player.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        PlayerFaction playerFaction = target.getFaction() != null && target.getFaction() instanceof PlayerFaction ? (PlayerFaction) target.getFaction() : null;
        if (playerFaction != null) {
            new Text(ConfigurationService.YELLOW + "   Faction: " + playerFaction.getDisplayName(player)).setHoverText(ConfigurationService.GRAY + "Click to view Faction").setClick(ClickAction.RUN_COMMAND, "/f who " + playerFaction.getName()).send(player);
        }
        int conquestCaps = 0;
        int kothCaps = 0;
        int citadelCaps = 0;
        int selfCaps = 0;
        for(EventCapture eventCapture: target.getEventCaptures()){
            switch (eventCapture.getEventType()){
                case CITADEL: {
                    citadelCaps++;
                    break;
                }
                case KOTH:{
                    kothCaps++;
                    if(eventCapture.getPlayer().equalsIgnoreCase(target.getName())){
                        selfCaps++;
                    }
                    break;
                }
                case CONQUEST:{
                    conquestCaps++;
                    break;
                }
            }
        }
        if(citadelCaps > 0){
            player.sendMessage(ConfigurationService.YELLOW + ChatColor.BOLD.toString() + "   Citadel Caps: " + ConfigurationService.GRAY + citadelCaps);
        }
        if(kothCaps > 0){
            player.sendMessage(ConfigurationService.YELLOW + "   KOTH Caps: " + ConfigurationService.GRAY + kothCaps + " (Self Captured: " + selfCaps + ")");
        }
        if(conquestCaps > 0){
            player.sendMessage(ConfigurationService.YELLOW + "   Conquest Caps: " + ConfigurationService.GRAY + conquestCaps);
        }
        if (!ConfigurationService.KIT_MAP) {
            Set<String> pastFactions = new HashSet<>(target.getPreviousFactions());
            if(target.getFaction() != null) {
                pastFactions.remove(target.getFaction().getName());
            }
            player.sendMessage(ConfigurationService.YELLOW + "   Previous Factions: " + ConfigurationService.GRAY + (pastFactions.isEmpty() ? "None" : Joiner.on(", ").join(pastFactions)));
            player.sendMessage(ConfigurationService.YELLOW + "   Kills: " + ConfigurationService.GRAY + target.getKills());
            player.sendMessage(ConfigurationService.YELLOW + "   Deaths: " + ConfigurationService.GRAY + target.getDeaths());
            player.sendMessage(ConfigurationService.YELLOW + "   Balance: " + ConfigurationService.GRAY + "$" + target.getBalance());
            player.sendMessage(ConfigurationService.YELLOW + "   PlayTime: " + ConfigurationService.GRAY + DurationFormatUtils.formatDurationWords(target.getPlaytime(), true, true));
            player.sendMessage(ConfigurationService.YELLOW + "   Diamonds Mined: " + ConfigurationService.GRAY + target.getDiamondsMined());
            player.sendMessage(ConfigurationService.YELLOW + "   Miner Level: " + ConfigurationService.GRAY +  target.getMinerLevel().getNick());
            player.sendMessage(ConfigurationService.YELLOW + "   Creepers Killed: " + ConfigurationService.GRAY + target.getCreepersKilled());
            player.sendMessage(ConfigurationService.YELLOW + "   Endermen Killed: " + ConfigurationService.GRAY + target.getEnderKilled());
            if ((target.getDeathban() != null) && (target.getDeathban().getRemaining() > 0L)) {
                new Text(ConfigurationService.YELLOW + "   Deathbanned: " + (target.getDeathban().isActive() ? ConfigurationService.RED + DurationFormatUtils.formatDurationWords(target.getDeathban().getRemaining(), true, true) : new StringBuilder().append(ConfigurationService.RED).append("false").toString())).setHoverText(ConfigurationService.GRAY + "Un-Deathbanned in: " + DurationFormatUtils.formatDurationWords(target.getDeathban().getRemaining(), true, true)).send(player);
            }
        } else {
            player.sendMessage(ConfigurationService.YELLOW + "   Kills" + ConfigurationService.GRAY + ": " + target.getKills());
            player.sendMessage(ConfigurationService.YELLOW + "   Deaths" + ConfigurationService.GRAY + ": " + target.getDeaths());
            player.sendMessage(ConfigurationService.YELLOW + "   KDR" + ConfigurationService.GRAY + ": " + new DecimalFormat("#.##").format(target.getKDR()));
            player.sendMessage(ConfigurationService.YELLOW + "   KillStreak" + ConfigurationService.GRAY + ": " + target.getKillStreak());
            player.sendMessage(ConfigurationService.YELLOW + "   Balance" + ConfigurationService.GRAY + ": $" + target.getBalance());
        }
        player.sendMessage(ChatColor.DARK_GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }
}
