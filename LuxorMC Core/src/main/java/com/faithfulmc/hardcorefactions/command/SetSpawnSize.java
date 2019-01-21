package com.faithfulmc.hardcorefactions.command;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.type.*;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.text.ParseException;

public class SetSpawnSize implements CommandExecutor{
    private final HCF hcf;

    public SetSpawnSize(HCF hcf) {
        this.hcf = hcf;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            if(args.length != 1){
                sender.sendMessage(ConfigurationService.YELLOW + "Invalid args: " + ConfigurationService.GRAY + "/" + label + " <size>");
            }
            else {
                double radius;
                try{
                    radius = NumberFormat.getInstance().parse(args[0]).doubleValue();
                }
                catch (ParseException ex){
                    sender.sendMessage(ConfigurationService.RED + "Invalid number");
                    return true;
                }
                Player player = (Player) sender;
                World world = player.getWorld();
                World.Environment environment = world.getEnvironment();
                ConfigurationService.SPAWN_RADIUS_MAP.put(environment, radius);
                SpawnFaction spawnFaction = (SpawnFaction)hcf.getFactionManager().getFaction("Spawn");
                spawnFaction.reset();
                NorthRoadFaction northRoadFaction = (NorthRoadFaction) hcf.getFactionManager().getFaction("NorthRoad");
                northRoadFaction.reset();
                EastRoadFaction eastRoadFaction = (EastRoadFaction) hcf.getFactionManager().getFaction("EastRoad");
                eastRoadFaction.reset();
                SouthRoadFaction southRoadFaction = (SouthRoadFaction) hcf.getFactionManager().getFaction("SouthRoad");
                southRoadFaction.reset();
                WestRoadFaction westRoadFaction = (WestRoadFaction) hcf.getFactionManager().getFaction("WestRoad");
                westRoadFaction.reset();
                sender.sendMessage(ConfigurationService.YELLOW + "Spawn region updated");
            }
        }
        else{
            sender.sendMessage(ConfigurationService.RED + "You must be a player to do this");
        }
        return true;
    }
}
