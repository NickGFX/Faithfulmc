package com.faithfulmc.hardcorefactions.faction.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.faction.FactionExecutor;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.timer.PlayerTimer;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;


public class FactionHomeArgument extends CommandArgument {
    private final FactionExecutor factionExecutor;
    private final HCF plugin;


    public FactionHomeArgument(FactionExecutor factionExecutor, HCF plugin) {
        super("home", "Teleport to the faction home.");
        if(ConfigurationService.ORIGINS){
            aliases = new String[]{"stuck"};
        }
        this.factionExecutor = factionExecutor;
        this.plugin = plugin;

    }


    public String getUsage(String label) {
        return '/' + label + ' ' + getName();

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;

        }
        Player player = (Player) sender;
        if ((args.length >= 2) && (args[1].equalsIgnoreCase("set"))) {
            this.factionExecutor.getArgument("sethome").onCommand(sender, command, label, args);
            return true;

        }

        java.util.UUID uuid = player.getUniqueId();

        PlayerTimer timer = this.plugin.getTimerManager().spawnTagTimer;
                long remaining = timer.getRemaining(player);

        if (remaining > 0L) {

            sender.sendMessage(ConfigurationService.RED + "You cannot warp whilst your " + timer.getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]");

            return true;

        }
        /*
        if ((remaining = (timer = this.plugin.getTimerManager().enderPearlTimer).getRemaining(player)) > 0L) {


            sender.sendMessage(ConfigurationService.RED + "You cannot warp whilst your " + timer.getDisplayName() + ConfigurationService.RED + " timer is active [" + ChatColor.BOLD + HCF.getRemaining(remaining, true, false) + ConfigurationService.RED + " remaining]");

            return true;
        }
        */
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(uuid);

        if (playerFaction == null) {

            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");

            return true;

        }

        org.bukkit.Location home = playerFaction.getHome();

        if (home == null) {

            sender.sendMessage(ConfigurationService.RED + "Your faction does not have a home set.");

            return true;

        }

        Faction factionAt = this.plugin.getFactionManager().getFactionAt(player.getLocation());

        if ((factionAt instanceof EventFaction)) {

            sender.sendMessage(ConfigurationService.RED + "You cannot warp whilst in event zones.");

            return true;

        } else if (!ConfigurationService.ORIGINS && factionAt instanceof PlayerFaction && playerFaction.getFactionRelation(factionAt) == Relation.ENEMY) {
            sender.sendMessage(ConfigurationService.RED + "You have to use /f stuck in enemy claims");
            return true;
        }
        else if(ConfigurationService.ORIGINS && factionAt.isSafezone() && player.getWorld().getEnvironment() == World.Environment.THE_END){
            sender.sendMessage(ConfigurationService.RED + "You may not /f home in the end safezone");
            return true;
        }

        long millis = 0L;

        if (factionAt.isSafezone()) {

            millis = 0L;

        } else {

            switch (player.getWorld().getEnvironment()) {

                case THE_END:
                    if(ConfigurationService.ORIGINS){
                        millis = 30000L;
                    }
                    else {
                        sender.sendMessage(ConfigurationService.RED + "You cannot teleport to your faction home whilst in The End.");
                        return true;
                    }
                    break;

                case NETHER:

                    millis = 30000L;

                    break;

                default:

                    millis = 10000L;

            }

        }

        if ((!factionAt.equals(playerFaction)) && ((factionAt instanceof PlayerFaction))) {

            millis *= 3L;

        }

        if (!ConfigurationService.KIT_MAP && this.plugin.getTimerManager().pvpProtectionTimer.getRemaining(player.getUniqueId()) > 0L) {

            player.sendMessage(ConfigurationService.RED + "You still have PvP Protection, you must enable it first.");

            return true;

        }

        if(plugin.getTimerManager().teleportTimer.getNearbyEnemies(player, 100) == 0){
            home.getChunk();
            player.teleport(home, PlayerTeleportEvent.TeleportCause.COMMAND);
            player.sendMessage(ConfigurationService.YELLOW + "You were teleported instantly because there were no enemies in a 100 block radius");
        }
        else {

            this.plugin.getTimerManager().teleportTimer.teleport(player, home, millis, ConfigurationService.YELLOW + "Teleporting to your faction home in " + ChatColor.LIGHT_PURPLE + HCF.getRemaining(millis, true, false) + ConfigurationService.YELLOW + ". Do not move or take damage.", org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
        }
        return true;

    }

}