package com.faithfulmc.framework.command;

public interface CommandManager {
    boolean containsCommand(final BaseCommand p0);

    void registerAll(final BaseCommandModule p0);

    void registerCommand(final BaseCommand p0);

    void registerCommands(final BaseCommand[] p0);

    void unregisterCommand(final BaseCommand p0);

    BaseCommand getCommand(final String p0);
}
