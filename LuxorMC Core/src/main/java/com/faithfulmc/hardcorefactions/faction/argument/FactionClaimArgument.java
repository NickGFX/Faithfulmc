package com.faithfulmc.hardcorefactions.faction.argument;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.claim.ClaimHandler;
import com.faithfulmc.hardcorefactions.faction.type.PlayerFaction;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class FactionClaimArgument extends CommandArgument {
    private final HCF plugin;

    public FactionClaimArgument(HCF plugin) {
        super("claim", "Claim land in the Wilderness.", new String[]{"claimland"});
        this.plugin = plugin;
    }

    public String getUsage(String label) {
        return "" + '/' + label + ' ' + this.getName();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigurationService.RED + "This command is only executable by players.");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        PlayerFaction playerFaction = this.plugin.getFactionManager().getPlayerFaction(uuid);
        if (playerFaction == null) {
            sender.sendMessage(ConfigurationService.RED + "You are not in a faction.");
            return true;
        }
        if (playerFaction.isRaidable()) {
            sender.sendMessage(ConfigurationService.RED + "You cannot claim land for your faction while raidable.");
            return true;
        }
        PlayerInventory inventory = player.getInventory();
        if (inventory.contains(ClaimHandler.CLAIM_WAND)) {
            sender.sendMessage(ConfigurationService.RED + "You already have a claiming wand in your inventory.");
            return true;
        }
        if (!inventory.addItem(new ItemStack[]{ClaimHandler.CLAIM_WAND}).isEmpty()) {
            sender.sendMessage(ConfigurationService.RED + "Your inventory is full.");
            return true;
        }
        sender.sendMessage(ConfigurationService.YELLOW + "Claiming wand added to inventory, read the item to understand how to claim. You can also" + (Object) ConfigurationService.YELLOW + " use " + (Object) ConfigurationService.GRAY + '/' + label + " claimchunk" + (Object) ConfigurationService.YELLOW + '.');
        return true;
    }
}