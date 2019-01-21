package com.faithfulmc.framework.command;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.util.command.ArgumentExecutor;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.regex.Pattern;

public abstract class BaseCommand extends ArgumentExecutor {
    private static final Pattern USAGE_REPLACER_PATTERN;

    public static boolean canSee(final CommandSender sender, final Player target) {
        if(BasePlugin.PRACTICE){
            return true;
        }
        return target != null && (!(sender instanceof Player) || ((Player) sender).canSee(target));
    }

    static {
        USAGE_REPLACER_PATTERN = Pattern.compile("(command)", 16);
    }

    private final String name;
    private final String description;
    private String[] aliases;
    private String[] flags;
    private String usage;

    public BaseCommand(final String name, final String description) {
        super(name);
        this.name = name;
        this.description = description;
    }

    public final String getPermission() {
        return "base.command." + this.name;
    }

    public boolean isPlayerOnlyCommand() {
        return false;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String[] getFlags() {
        return this.flags;
    }

    protected void setFlags(final String[] flags) {
        this.flags = flags;
    }

    public String getUsage() {
        if (this.usage == null) {
            this.usage = "";
        }
        return BaseCommand.USAGE_REPLACER_PATTERN.matcher(this.usage).replaceAll(this.name);
    }

    public void setUsage(final String usage) {
        this.usage = usage;
    }

    public String getUsage(final String label) {
        return ChatColor.RED + "Usage: " + BaseCommand.USAGE_REPLACER_PATTERN.matcher(this.usage).replaceAll(label);
    }

    public String[] getAliases() {
        if (this.aliases == null) {
            this.aliases = ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return Arrays.copyOf(this.aliases, this.aliases.length);
    }

    protected void setAliases(final String[] aliases) {
        this.aliases = aliases;
    }
}
