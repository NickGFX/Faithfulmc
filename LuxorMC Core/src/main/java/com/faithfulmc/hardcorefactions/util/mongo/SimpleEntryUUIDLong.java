package com.faithfulmc.hardcorefactions.util.mongo;

import com.google.common.base.Preconditions;
import org.mongodb.morphia.annotations.Embedded;

import java.util.UUID;

@Embedded
public class SimpleEntryUUIDLong{
    private UUID key;
    private long value;

    public SimpleEntryUUIDLong() {
    }

    public SimpleEntryUUIDLong(UUID key, Long value) {
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

    public Long getValue() {
        return value;
    }

    public Long setValue(Object value) {
        Preconditions.checkNotNull(value);
        long oldvalue = this.value;
        this.value = (Integer) value;
        return oldvalue;
    }
}
