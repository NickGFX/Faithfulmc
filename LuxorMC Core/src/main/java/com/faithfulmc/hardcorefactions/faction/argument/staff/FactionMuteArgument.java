package com.faithfulmc.hardcorefactions.faction.argument.staff;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.JavaUtils;
import com.faithfulmc.util.command.CommandArgument;
import net.minecraft.util.org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class FactionMuteArgument extends CommandArgument {
    private final HCF hcf;

    public FactionMuteArgument(HCF hcf) {
        super("mute", "Mutes a faction in global chat");
        this.permission = "hcf.command.faction.argument." + this.getName();
        this.hcf = hcf;
    }

    public String getUsage(final String label) {
        return '/' + label + ' ' + this.getName() + " <playerName/factionName> [duration]";
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
                if (playerFaction.isMuted()) {
                    sender.sendMessage(ConfigurationService.RED + "That faction is already muted");
                } else {
                    String duration = null;
                    for (int i = 2; i < args.length; i++) {
                        String arg = args[i];
                        if (!arg.equals("-s")) {
                            duration = arg;
                        }
                    }
                    final long time;
                    if (duration != null) {
                        time = JavaUtils.parse(duration);
                        if (time == -1L) {
                            sender.sendMessage(ConfigurationService.RED + "Invalid duration, use the correct format: 10m 1s");
                            return;
                        }
                    } else {
                        time = -1;
                    }
                    String time_formatted = DurationFormatUtils.formatDurationWords(time, true, true);
                    boolean silent = Arrays.asList(args).contains("-s");
                    if (!silent) {
                        playerFaction.broadcast(ConfigurationService.GOLD + "Your faction was " + (time == -1 ? "" : "temporarily ") + "muted by " + ConfigurationService.WHITE + sender.getName() + (time == -1 ? "" : ConfigurationService.GOLD + " for " + time_formatted));
                    }
                    playerFaction.setMuted(true);
                    playerFaction.setMutetime(time == -1 ? -1 : System.currentTimeMillis() + time);
                    String msg = (silent ? ConfigurationService.GRAY + "(Unlisted) " : "") + ChatColor.GREEN + playerFaction.getName() + " was " + (time == -1 ? "" : "temporarily ") + "muted by " + sender.getName() + (time == -1 ? "" : " for " + time_formatted);
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
