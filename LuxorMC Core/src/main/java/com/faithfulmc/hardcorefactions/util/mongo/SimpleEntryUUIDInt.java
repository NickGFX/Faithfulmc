package com.faithfulmc.hardcorefactions.util.mongo;

import com.google.common.base.Preconditions;
import org.mongodb.morphia.annotations.Embedded;

import java.util.UUID;

@Embedded
public class SimpleEntryUUIDInt{
    private UUID key;
    private int value;

    public SimpleEntryUUIDInt() {
}

    public SimpleEntryUUIDInt(UUID key, Integer value) {
        Preconditions.checkNotNull(value);
        this.key = key;
        this.value = value;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public Integer getValue() {
        return value;
    }

    public Integer setValue(Object value) {
        Preconditions.checkNotNull(value);
        int oldvalue = this.value;
        this.value = (Integer) value;
        return oldvalue;
    }
}
