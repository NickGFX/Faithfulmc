package com.faithfulmc.framework.command.module;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.command.BaseCommandModule;
import com.faithfulmc.framework.command.module.inventory.*;

public class InventoryModule extends BaseCommandModule {
    public InventoryModule(final BasePlugin plugin) {
        this.commands.add(new ClearInvCommand());
        this.commands.add(new GiveCommand());
        this.commands.add(new IdCommand());
        this.commands.add(new InvSeeCommand(plugin));
        this.commands.add(new ItemCommand());
        this.commands.add(new SkullCommand());
        this.commands.add(new CopyInvCommand());
    }
}
