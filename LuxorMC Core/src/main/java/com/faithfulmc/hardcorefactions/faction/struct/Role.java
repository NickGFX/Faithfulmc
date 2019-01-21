package com.faithfulmc.hardcorefactions.faction.struct;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public enum Role {
    LEADER("Leader", "**"), COLEADER("Co-Leader", "*"), CAPTAIN("Captain", "+"), MEMBER("Member", "");

    private final String name;
    private final String astrix;

    private Role(String name, String astrix) {
        this.name = name;
        this.astrix = astrix;
    }

    public String getName() {
        return this.name;
    }

    public String getAstrix() {
        return this.astrix;
    }
}
