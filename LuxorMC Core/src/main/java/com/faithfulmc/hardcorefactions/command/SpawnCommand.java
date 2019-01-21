package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.events.faction.EventFaction;
import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import com.faithfulmc.hardcorefactions.faction.type.Faction;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.hardcorefactions.user.FactionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SpawnCommand implements CommandExecutor, TabCompleter {
    private static final int KIT_MAP_TELEPORT_DELAY = 45;
    private HCF plugin;
    private ConversationFactory factory;

    public SpawnCommand(HCF plugin) {
        this.plugin = plugin;
        factory = new ConversationFactory(plugin).withTimeout(15).withEscapeSequence("/no").withFirstPrompt(new SpawnTeleportPrompt()).withModality(false).withLocalEcho(false);
    }

    public class SpawnTeleportPrompt extends StringPrompt {
        private static final int TIME = 30;

        public String getPromptText(ConversationContext conversationContext) {
            return ConfigurationService.YELLOW + "Are you sure you want to spend a credit? Type " + ChatColor.GREEN + "yes" + ConfigurationService.YELLOW + " to continue or " + ConfigurationService.RED + "no" + ConfigurationService.YELLOW + " to cancel.";
        }

        public Prompt acceptInput(ConversationContext conversationContext, String s) {
            Player player = (Player) conversationContext.getForWhom();
            switch (s.toLowerCase()){
                case "yes":
                    FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
                    if(factionUser.getSpawncredits() > 0){
                        factionUser.setSpawncredits(factionUser.getSpawncredits() - 1);
                        plugin.getTimerManager().teleportTimer.teleport(
                                player,
                                player.getWorld().getSpawnLocation().add(0.5, 0.5, 0.5),TimeUnit.SECONDS.toMillis(TIME),
                                ConfigurationService.YELLOW + "You will be teleported to spawn in " + TIME + " seconds, do not move or take damage or your credit will be lost.",
                                PlayerTeleportEvent.TeleportCause.COMMAND);
                    }
                    else{
                        player.sendMessage(ConfigurationService.YELLOW + "You do not have enough spawn credits for this");
                    }
                    break;
                default:
                    player.sendMessage(ConfigurationService.YELLOW + "Teleportation cancelled");
                    break;
            }
            return Prompt.END_OF_CONVERSATION;
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage( ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        Player player = (Player) sender;
        World world = player.getWorld();
        Location spawn = world.getSpawnLocation().clone().add(0.5, 0.5, 0.5);
        if (!ConfigurationService.KIT_MAP && !sender.hasPermission(command.getPermission() + ".teleport")) {
            FactionUser factionUser = plugin.getUserManager().getUser(player.getUniqueId());
            if(world.getEnvironment() == World.Environment.THE_END || factionUser.getSpawncredits() == 0) {
                sender.sendMessage( ConfigurationService.YELLOW + "You must travel to Spawn " + ConfigurationService.GRAY + "(0, 0)");
                sender.sendMessage(ConfigurationService.YELLOW + "If you wish to buy a spawncredit to travel to spawn visit " + ConfigurationService.GOLD + ConfigurationService.STORE);
            }
            else{
                PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
                Faction factionAt = this.plugin.getFactionManager().getFactionAt(player.getLocation());
                if ((factionAt instanceof EventFaction)) {
                    sender.sendMessage(ConfigurationService.RED + "You cannot warp whilst in event zones.");
                } else if (factionAt instanceof PlayerFaction && playerFaction.getFactionRelation(factionAt) == Relation.ENEMY) {
                    sender.sendMessage(ConfigurationService.RED + "You may not use this in enemy claims");
                }
                else if(plugin.getTimerManager().teleportTimer.getNearbyEnemies(player, 20) != 0){
                    sender.sendMessage(ConfigurationService.RED + "You may not warp whilst enemies are nearby");
                }
                else {
                    player.beginConversation(factory.buildConversation(player));
                }
            }
            return true;
        }
        if (!sender.hasPermission(command.getPermission() + ".teleport")) {
            PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
            Faction factionAt = this.plugin.getFactionManager().getFactionAt(player.getLocation());
            if ((factionAt instanceof EventFaction)) {
                sender.sendMessage(ConfigurationService.RED + "You cannot warp whilst in event zones.");
                return true;
            } else if (factionAt != null && factionAt instanceof PlayerFaction && factionAt.getFactionRelation(playerFaction) == Relation.ENEMY) {
                sender.sendMessage(ConfigurationService.RED + "You have to use /f stuck in enemy claims");
                return true;
            }
            this.plugin.getTimerManager().teleportTimer.teleport(player, Bukkit.getWorld("world").getSpawnLocation(), TimeUnit.SECONDS.toMillis(KIT_MAP_TELEPORT_DELAY), ConfigurationService.YELLOW + "Teleporting to spawn in " + KIT_MAP_TELEPORT_DELAY + " seconds.", PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        }
        if (args.length > 0) {
            world = Bukkit.getWorld( args[0]);
            if (world == null) {
                sender.sendMessage( ConfigurationService.RED + "There is not a world named " + args[0] + '.');
                return true;
            }
            spawn = world.getSpawnLocation().clone().add(0.5, 0.0, 0.5);
        }
        player.teleport(spawn, PlayerTeleportEvent.TeleportCause.COMMAND);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
