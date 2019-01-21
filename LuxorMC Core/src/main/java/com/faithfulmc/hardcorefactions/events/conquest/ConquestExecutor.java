package com.faithfulmc.hardcorefactions.events.conquest;


import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.util.command.ArgumentExecutor;


public class ConquestExecutor extends ArgumentExecutor {

    public ConquestExecutor(HCF plugin) {
        super("conquest");
        addArgument(new ConquestSetpointsArgument(plugin));

    }

}