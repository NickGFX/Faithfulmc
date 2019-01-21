package com.faithfulmc.hardcorefactions.util.mongo;

import com.google.common.base.Preconditions;
import org.mongodb.morphia.annotations.Embedded;

@Deprecated
@Embedded
public class SimpleEntryObjectLong<K>{
    @Embedded
    private K key;
    @Embedded
    private long value;

    public SimpleEntryObjectLong() {
    }

    public SimpleEntryObjectLong(K key, Long value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
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
