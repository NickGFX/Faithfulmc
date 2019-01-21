package com.faithfulmc.framework.user.mongo;

import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.user.BaseUser;
import com.faithfulmc.framework.user.ServerParticipator;
import com.faithfulmc.framework.user.UserManager;
import org.bson.Document;
import org.mongodb.morphia.Datastore;

import java.util.UUID;
import java.util.regex.Pattern;

public class MongoUserManager extends UserManager {
    private Datastore datastore;

    public MongoUserManager(BasePlugin plugin) {
        super(plugin);
        datastore = plugin.getDatastore();
    }

    public void save(BaseUser baseUser) {
        datastore.save(baseUser);
    }

    public BaseUser load(UUID uuid) {
        return datastore.find(BaseUser.class).field("_id").equal(uuid).get();
    }

    public boolean exists(String name){
        Pattern pattern = Pattern.compile("^" + name + "$", Pattern.CASE_INSENSITIVE);
        return datastore.find(ServerParticipator.class).field("name").equal(pattern).iterator().hasNext();
    }

    public void mongoFetch(UUID uuid){
        if(onlinePlayers.containsKey(uuid)){
            BaseUser newUser = load(uuid);
            BaseUser old = (BaseUser) onlinePlayers.put(uuid, newUser);
            if(old != null) {
                old.merge(newUser);
            }
        }
    }
}
