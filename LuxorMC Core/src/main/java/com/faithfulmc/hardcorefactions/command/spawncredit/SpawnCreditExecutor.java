package com.faithfulmc.hardcorefactions.command.spawncredit;

import com.faithfulmc.hardcorefactions.HCF;
import com.faithfulmc.hardcorefactions.command.spawncredit.argument.SpawnCreditCheckArgument;
import com.faithfulmc.hardcorefactions.command.spawncredit.argument.SpawnCreditGiveArgument;
import com.faithfulmc.hardcorefactions.command.spawncredit.argument.SpawnCreditSetArgument;
import com.faithfulmc.util.command.ArgumentExecutor;

public class SpawnCreditExecutor extends ArgumentExecutor{
    public SpawnCreditExecutor(HCF hcf) {
        super("spawncredit");
        addArgument(new SpawnCreditCheckArgument(hcf));
        addArgument(new SpawnCreditGiveArgument(hcf));
        addArgument(new SpawnCreditSetArgument(hcf));
    }
}
