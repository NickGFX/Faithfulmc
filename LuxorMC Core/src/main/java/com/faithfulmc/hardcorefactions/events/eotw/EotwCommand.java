package com.faithfulmc.hardcorefactions.events.eotw;

import com.faithfulmc.hardcorefactions.ConfigurationService;
import com.faithfulmc.hardcorefactions.HCF;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.conversations.*;

import java.util.Collections;
import java.util.List;

public class EotwCommand implements CommandExecutor, TabCompleter {
    private final ConversationFactory factory;

    public EotwCommand(final HCF plugin) {
        this.factory = new ConversationFactory(plugin).withFirstPrompt(new EotwPrompt()).withEscapeSequence("/no").withTimeout(10).withModality(false).withLocalEcho(true);
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ConfigurationService.RED + "This command can be only executed from console.");
            return true;
        }
        final Conversable conversable = (Conversable) sender;
        conversable.beginConversation(this.factory.buildConversation(conversable));
        return true;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        return Collections.emptyList();
    }

    private static final class EotwPrompt extends StringPrompt {
        public String getPromptText(final ConversationContext context) {
            return ConfigurationService.YELLOW + "Are you sure you want to do this? The server will be in EOTW mode, If EOTW mode is active, all claims whilst making Spawn a KOTH. " + "You will still have " + EotwHandler.EOTW_WARMUP_WAIT_SECONDS + " seconds to cancel this using the same command though. " + "Type " + ChatColor.GREEN + "yes" + ConfigurationService.YELLOW + " to confirm or " + ConfigurationService.RED + "no" + ConfigurationService.YELLOW + " to deny.";
        }

        public Prompt acceptInput(final ConversationContext context, final String string) {
            if (string.equalsIgnoreCase("yes")) {
                final boolean newStatus = !HCF.getInstance().getEotwHandler().isEndOfTheWorld(false);
                final Conversable conversable = context.getForWhom();
                if (conversable instanceof CommandSender) {
                    Command.broadcastCommandMessage((CommandSender) conversable, ConfigurationService.GOLD + "Set EOTW mode to " + newStatus + '.');
                } else {
                    conversable.sendRawMessage(ConfigurationService.GOLD + "Set EOTW mode to " + newStatus + '.');
                }
                HCF.getInstance().getEotwHandler().setEndOfTheWorld(newStatus);
            } else if (string.equalsIgnoreCase("no")) {
                context.getForWhom().sendRawMessage(ChatColor.BLUE + "Cancelled the process of setting EOTW mode.");
            } else {
                context.getForWhom().sendRawMessage(ConfigurationService.RED + "Unrecognized response. Process of toggling EOTW mode has been cancelled.");
            }
            return Prompt.END_OF_CONVERSATION;
        }
    }
}