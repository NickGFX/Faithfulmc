package com.faithfulmc.hardcorefactions.scoreboard;

import com.faithfulmc.tablist.tab.DynamicRow;
import com.faithfulmc.tablist.tab.DynamicTabRow;

import java.util.function.Supplier;

public class FunctionTabRow extends DynamicTabRow implements DynamicRow{
    private final Supplier<String> getPrefix;
    private final Supplier<String> getSuffix;

    public FunctionTabRow(String rowString, Supplier<String> prefix, Supplier<String> suffix) {
        super(rowString);
        getPrefix = prefix;
        getSuffix = suffix;
    }

    @Override
    public String getPrefix() {
        return getPrefix.get();
    }

    @Override
    public String getSuffix() {
        return getSuffix.get();
    }
}
