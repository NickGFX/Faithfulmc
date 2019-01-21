package com.faithfulmc.util.morphia;

import com.mongodb.DBObject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MappingException;
import org.mongodb.morphia.mapping.cache.EntityCache;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomAllowMapper implements org.mongodb.morphia.mapping.CustomMapper {
    private Logger logger;
    private org.mongodb.morphia.mapping.CustomMapper previous;

    public CustomAllowMapper(org.mongodb.morphia.mapping.CustomMapper previous, Logger logger) {
        this.previous = previous;
        this.logger = logger;
    }

    public void fromDBObject(Datastore datastore, DBObject dbObject, MappedField mappedField, Object o, EntityCache entityCache, Mapper mapper) {
        try {
            previous.fromDBObject(datastore, dbObject, mappedField, o, entityCache, mapper);
        } catch (Exception e) {
            logger.log(Level.INFO, "Forced Morphia to accept field ", e);
        }
    }

    public void toDBObject(Object o, MappedField mappedField, DBObject dbObject, Map<Object, DBObject> map, Mapper mapper) {
        try {
            previous.toDBObject(o, mappedField, dbObject, map, mapper);
        } catch (Exception e) {
            logger.log(Level.INFO, "Forced Morphia to avoid field ", e);
        }
    }
}
