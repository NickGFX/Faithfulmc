package com.faithfulmc.hardcorefactions.command.lives;


import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.command.lives.argument.*;
import com.faithfulmc.util.command.ArgumentExecutor;


public class LivesExecutor extends ArgumentExecutor {

    public LivesExecutor(HCF plugin) {
        super("lives");
        addArgument(new LivesCheckArgument(plugin));
        addArgument(new LivesCheckDeathbanArgument(plugin));
        addArgument(new LivesDeathBanHistoryArgument(plugin));
        addArgument(new LivesClearDeathbansArgument(plugin));
        addArgument(new LivesGiveArgument(plugin));
        addArgument(new LivesReviveArgument(plugin));
        addArgument(new LivesSetArgument(plugin));
        addArgument(new LivesSetDeathbanTimeArgument());
    }

}