package com.faithfulmc.hardcorefactions.util.mongo;

import org.mongodb.morphia.annotations.Embedded;

@Deprecated
@Embedded
public class SimpleEntryObjectObject<K,V>{
    @Embedded
    private K key;
    @Embedded
    private V value;

    public SimpleEntryObjectObject(){

    }

    public SimpleEntryObjectObject(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public V setValue(Object value) {
        V oldValue = this.value;
        this.value = (V) value;
        return oldValue;
    }
}
