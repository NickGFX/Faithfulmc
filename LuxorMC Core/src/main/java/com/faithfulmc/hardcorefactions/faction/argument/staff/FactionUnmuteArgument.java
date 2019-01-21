package com.faithfulmc.hardcorefactions.faction.argument.staff;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class FactionUnmuteArgument extends CommandArgument {
    private final HCF hcf;

    public FactionUnmuteArgument(HCF hcf) {
        super("unmute", "Unmutes a faction in global chat");
        this.permission = "hcf.command.faction.argument." + this.getName();
        this.hcf = hcf;
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <playerName/factionName>";
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        new BukkitRunnable() {
            public void run() {
                if (args.length <= 1) {
                    sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));
                    return;
                }
                PlayerFaction playerFaction = hcf.getFactionManager().getContainingPlayerFaction(args[1]);
                if (playerFaction == null) {
                    Faction faction = hcf.getFactionManager().getFaction(args[1]);
                    if (faction == null || !(faction instanceof PlayerFaction)) {
                        sender.sendMessage(ConfigurationService.RED + "No player or faction with that name was found");
                        return;
                    }
                    playerFaction = (PlayerFaction) faction;
                }
                if (!playerFaction.isMuted()) {
                    sender.sendMessage(ConfigurationService.RED + "That faction is already unmuted");
                } else {
                    boolean silent = Arrays.asList(args).contains("-s");
                    if (!silent) {
                        playerFaction.broadcast(ConfigurationService.GOLD + "Your faction was unmuted by " + ConfigurationService.WHITE + sender.getName());
                    }
                    playerFaction.setMuted(false);
                    playerFaction.setMutetime(0);
                    String msg = (silent ? ConfigurationService.GRAY + "(Unlisted) " : "") + ChatColor.GREEN + playerFaction.getName() + " was unmuted by " + sender.getName();
                    sender.sendMessage(msg);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player != sender && player.hasPermission(getPermission())) {
                            player.sendMessage(msg);
                        }
                    }
                }
            }
        }.runTaskAsynchronously(hcf);
        return true;
    }
}
