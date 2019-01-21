package com.faithfulmc.hardcorefactions.command;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import net.minecraft.util.io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.util.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;


public class HubCommand implements CommandExecutor {

    public static void teleport(Player pl, String input) {

        ByteArrayOutputStream b = new ByteArrayOutputStream();

        DataOutputStream out = new DataOutputStream(b);

        try {

            out.writeUTF("Connect");

            out.writeUTF(input);

        } catch (IOException localIOException) {
            localIOException.printStackTrace();
        }

        pl.sendPluginMessage(HCF.getInstance(), "BungeeCord", b.toByteArray());

    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }

        Player player = (Player) sender;

        player.sendMessage(ConfigurationService.YELLOW + "You are being sent to the hub");
        sendToHub(player);
        return false;

    }

    public static void sendToHub(Player player){
        String hub = "Hub" + (ThreadLocalRandom.current().nextInt(ConfigurationService.HUBS) + 1);
        teleport(player, hub);
    }

}
