package com.faithfulmc.hardcorefactions.util.mongo;

import com.google.common.base.Preconditions;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class SimpleEntryIntInt {
    private int key;
    private int value;

    public SimpleEntryIntInt() {
    }

    public SimpleEntryIntInt(Integer key, Integer value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public Integer getValue() {
        return value;
    }

    public Integer setValue(Object value) {
        int lastValue = this.value;
        this.value = (Integer) value;
        return lastValue;
    }
}
