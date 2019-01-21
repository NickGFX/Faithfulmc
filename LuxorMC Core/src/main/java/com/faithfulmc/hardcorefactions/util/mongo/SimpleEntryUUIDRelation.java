package com.faithfulmc.hardcorefactions.util.mongo;

import com.faithfulmc.hardcorefactions.faction.struct.Relation;
import org.mongodb.morphia.annotations.Embedded;

import java.util.UUID;

@Embedded
public class SimpleEntryUUIDRelation{
    @Embedded
    private UUID key;
    @Embedded
    private Relation value;

    public SimpleEntryUUIDRelation(){

    }

    public SimpleEntryUUIDRelation(UUID key, Relation value) {
        this.key = key;
        this.value = value;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public Relation getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public Relation setValue(Object value) {
        Relation oldRelationalue = this.value;
        this.value = (Relation) value;
        return oldRelationalue;
    }
}
