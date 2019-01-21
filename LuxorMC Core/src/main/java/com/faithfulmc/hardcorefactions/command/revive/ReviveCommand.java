package com.faithfulmc.hardcorefactions.command.revive;

public class ReviveCommand {
    private final String name;
    private final String permission;
    private final String prefix;
    private final String display;
    private final long cooldown;

    public ReviveCommand(String name, String permission, String prefix, String display, long cooldown) {
        this.name = name;
        this.permission = permission;
        this.prefix = prefix;
        this.display = display;
        this.cooldown = cooldown;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDisplay() {
        return display;
    }

    public long getCooldown() {
        return cooldown;
    }
}
