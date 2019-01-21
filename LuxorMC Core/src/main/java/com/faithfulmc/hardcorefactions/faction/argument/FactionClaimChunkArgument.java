package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.Claim;
import com.faithfulmc.hardcorefactions.faction.struct.Role;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionClaimChunkArgument extends CommandArgument {
    private static final int CHUNK_RADIUS = 7;
    private final HCF plugin;

    public FactionClaimChunkArgument(HCF plugin) {
        super("claimchunk", "Claim a chunk of land in the Wilderness.", new String[]{"chunkclaim"});
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage((Object) ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        Player player = (Player) sender;
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            sender.sendMessage((Object) ConfigurationService.RED + "You are not in a faction.");
            return true;
        }
        if (playerFaction.isRaidable()) {
            sender.sendMessage((Object) ConfigurationService.RED + "You cannot claim land for your faction while raidable.");
            return true;
        }
        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            sender.sendMessage((Object) ConfigurationService.RED + "You must be an officer to claim land.");
            return true;
        }
        Location location = player.getLocation();
        this.plugin.getClaimHandler().tryPurchasing(player, new Claim(playerFaction, location.clone().add(CHUNK_RADIUS, 0.0, CHUNK_RADIUS), location.clone().add(-CHUNK_RADIUS, 256.0, -CHUNK_RADIUS)));
        return true;
    }
}