package com.faithfulmc.framework.command.module;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommandModule;
import com.faithfulmc.framework.command.module.teleport.*;
import com.faithfulmc.framework.command.module.warp.WarpExecutor;

public class TeleportModule extends BaseCommandModule {
    public TeleportModule(final BasePlugin plugin) {
        if(!BasePlugin.PRACTICE) {
            this.commands.add(new TeleportAllCommand());
        }
        this.commands.add(new TeleportCommand());
        this.commands.add(new TeleportHereCommand());
        this.commands.add(new TopCommand());
        this.commands.add(new WorldCommand());
        this.commands.add(new WarpExecutor(plugin));
    }
}
