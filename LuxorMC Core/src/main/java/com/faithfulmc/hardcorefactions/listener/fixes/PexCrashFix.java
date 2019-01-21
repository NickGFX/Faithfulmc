package com.faithfulmc.hardcorefactions.listener.fixes;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PexCrashFix implements Listener {
    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent e) {
        if ((!e.getPlayer().isOp()) || (!e.getPlayer().hasPermission("pex.bypass"))) {
            String cmd = e.getMessage().toLowerCase().replaceFirst("/", "");
            if ((cmd.startsWith("pex")) || (cmd.startsWith("permission")) || (((cmd.contains("faction")) || (cmd.contains("f"))) && ((cmd.contains("top")) || (cmd.contains("t"))) && ((cmd.contains("balance")) || (cmd.contains("money"))))) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ConfigurationService.RED + "You lack the correct permissions to run this command!");
            }
        }
        String cmd = e.getMessage().toLowerCase().replaceFirst("/", "");
        if (cmd.startsWith("pex ") && !e.isCancelled()) {
            String[] args = cmd.substring("pex ".length()).split(" ");
            if (args.length == 1 && args[0].equalsIgnoreCase("user")) {
                e.getPlayer().sendMessage(ConfigurationService.RED + "That could crash pex");
                e.setCancelled(true);
            }
        }
    }
}
