package com.faithfulmc.hardcorefactions.faction.argument;


import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.struct.ChatChannel;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;


public class FactionMessageArgument extends CommandArgument {
    private final HCF plugin;


    public FactionMessageArgument(HCF plugin) {

        super("message", "Sends a message to your faction.");

        this.plugin = plugin;

        this.aliases = new String[]{"msg"};

    }


    public String getUsage(String label) {

        return '/' + label + ' ' + getName() + " <message>";

    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {

            sender.sendMessage(ConfigurationService.RED + "Only players can use faction chat.");

            return true;

        }

        if (args.length < 2) {

            sender.sendMessage(ConfigurationService.RED + "Usage: " + getUsage(label));

            return true;

        }

        Player player = (Player) sender;

        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);

        if (playerFaction == null) {

            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");

            return true;

        }

        String format = String.format(ChatChannel.FACTION.getRawFormat(player), new Object[]{"", StringUtils.join((Object[]) args, ' ', 1, args.length)});

        Iterator<Player> iterator = playerFaction.getOnlinePlayers().iterator();

        while (iterator.hasNext()) {

            Player target = (Player) iterator.next();

            target.sendMessage(format);

        }

        return true;

    }

}