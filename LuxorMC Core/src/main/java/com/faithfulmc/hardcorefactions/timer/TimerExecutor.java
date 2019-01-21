package com.faithfulmc.hardcorefactions.timer;


import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.timer.argument.TimerCheckArgument;
import com.faithfulmc.hardcorefactions.timer.argument.TimerSetArgument;
import com.faithfulmc.util.command.ArgumentExecutor;


public class TimerExecutor extends ArgumentExecutor {

    public TimerExecutor(HCF plugin) {
        super("timer");
        addArgument(new TimerCheckArgument(plugin));
        addArgument(new TimerSetArgument(plugin));

    }

}