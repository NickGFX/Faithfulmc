package com.faithfulmc.hardcorefactions.faction;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.faction.argument.*;
import com.faithfulmc.hardcorefactions.faction.argument.staff.*;
import com.faithfulmc.util.command.ArgumentExecutor;
import com.faithfulmc.util.command.CommandArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class FactionExecutor extends ArgumentExecutor {
    private final CommandArgument helpArgument;

    public FactionExecutor(HCF plugin) {
        super("faction");
        if (!ConfigurationService.KIT_MAP) {
            addArgument(new FactionSetDtrArgument(plugin));
            addArgument(new FactionSetDeathbanMultiplierArgument(plugin));
            addArgument(new FactionSetDtrRegenArgument(plugin));
        }
        addArgument(new FactionDepositArgument(plugin));
        addArgument(new FactionClaimArgument(plugin));
        addArgument(new FactionClaimChunkArgument(plugin));
        addArgument(new FactionClaimForArgument(plugin));
        addArgument(new FactionClaimsArgument(plugin));
        addArgument(new FactionClearClaimsArgument(plugin));
        addArgument(new FactionOpenArgument(plugin));
        addArgument(new FactionCoLeaderArgument(plugin));
        addArgument(new FactionAcceptArgument(plugin));
        addArgument(new FactionMapArgument(plugin));
        addArgument(new FactionWithdrawArgument(plugin));
        addArgument(new FactionHomeArgument(this, plugin));
        addArgument(new FactionSetHomeArgument(plugin));
        addArgument(new FactionUnclaimArgument(plugin));
        if (ConfigurationService.MAX_ALLIES_PER_FACTION > 0) {
            addArgument(new FactionAllyArgument(plugin));
        }
        addArgument(new FactionChatArgument(plugin));
        addArgument(new FactionChatSpyArgument(plugin));
        addArgument(new FactionCreateArgument(plugin));
        addArgument(new FactionDemoteArgument(plugin));
        addArgument(new FactionDisbandArgument(plugin));
        addArgument(new FactionForceJoinArgument(plugin));
        addArgument(new FactionForceKickArgument(plugin));
        addArgument(new FactionForceLeaderArgument(plugin));
        addArgument(new FactionForcePromoteArgument(plugin));
        addArgument(this.helpArgument = new FactionHelpArgument(this));
        addArgument(new FactionInviteArgument(plugin));
        addArgument(new FactionInvitesArgument(plugin));
        addArgument(new FactionKickArgument(plugin));
        addArgument(new FactionLeaderArgument(plugin));
        addArgument(new FactionLeaveArgument(plugin));
        addArgument(new FactionListArgument(plugin));
        addArgument(new FactionMessageArgument(plugin));
        addArgument(new FactionRemoveArgument(plugin));
        addArgument(new FactionRenameArgument(plugin));
        addArgument(new FactionPromoteArgument(plugin));
        addArgument(new FactionShowArgument(plugin));
        if(!ConfigurationService.ORIGINS) {
            addArgument(new FactionStuckArgument(plugin));
        }
        addArgument(new FactionSetPointsArgument(plugin));
        addArgument(new FactionAddPointsArgument(plugin));
        addArgument(new FactionSubtractPointsArgument(plugin));
        addArgument(new FactionTopArgument());
        addArgument(new FactionStatsArgument(plugin));
        addArgument(new FactionUnallyArgument(plugin));
        addArgument(new FactionUninviteArgument(plugin));
        addArgument(new FactionMuteArgument(plugin));
        addArgument(new FactionUnmuteArgument(plugin));
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            this.helpArgument.onCommand(sender, command, label, args);
            return true;
        }
        CommandArgument argument = getArgument(args[0]);
        if (argument != null) {
            String permission = argument.getPermission();
            if ((permission == null) || (sender.hasPermission(permission))) {
                argument.onCommand(sender, command, label, args);
                return true;
            }
        }
        this.helpArgument.onCommand(sender, command, label, args);
        return true;
    }
}
