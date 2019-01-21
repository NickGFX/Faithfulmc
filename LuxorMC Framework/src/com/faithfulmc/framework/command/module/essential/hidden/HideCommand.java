package com.faithfulmc.framework.command.module.essential.hidden;

import com.faithfulmc.framework.command.BaseCommand;
import com.faithfulmc.framework.command.module.essential.hidden.arguments.HiddenCreate;
import com.faithfulmc.framework.command.module.essential.hidden.arguments.HiddenList;
import com.faithfulmc.framework.command.module.essential.hidden.arguments.HiddenRemoveAll;

public class HideCommand extends BaseCommand{
    public HideCommand() {
        super("hide", "Command used for managing hidden areas");
        addArgument(new HiddenCreate());
        addArgument(new HiddenList());
        addArgument(new HiddenRemoveAll());
    }
}
