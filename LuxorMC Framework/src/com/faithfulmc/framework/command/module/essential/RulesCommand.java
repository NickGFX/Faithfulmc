package com.faithfulmc.framework.command.module.essential;

import com.faithfulmc.framework.BaseConstants;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.util.BukkitUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RulesCommand extends BaseCommand {
    private final BasePlugin plugin;

    public RulesCommand(final BasePlugin plugin) {
        super("rules", "Shows the server rules.");
        this.setUsage("/(command)");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        sender.sendMessage(BaseConstants.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(BaseConstants.GOLD + "Check our forums for rule information, " + BaseConstants.GRAY + BaseConstants.SITE + "/forum");
        sender.sendMessage(BaseConstants.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        return true;
    }
}
