package com.faithfulmc.hardcorefactions.util.mongo;

import com.google.common.base.Preconditions;
import org.mongodb.morphia.annotations.Embedded;

@Deprecated
@Embedded
public class SimpleEntryObjectInt<K>{
    @Embedded
    private K key;
    @Embedded
    private int value;

    public SimpleEntryObjectInt() {
    }

    public SimpleEntryObjectInt(K key, Integer value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
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
