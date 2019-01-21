package com.faithfulmc.framework.user;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;
import java.util.UUID;

@org.mongodb.morphia.annotations.Entity(value = "globaluser")
public class ConsoleUser extends ServerParticipator implements ConfigurationSerializable {
    public static final UUID CONSOLE_UUID;

    static {
        CONSOLE_UUID = UUID.fromString("29f26148-4d55-4b4b-8e07-900fda686a67");
    }


    public ConsoleUser() {
        super(ConsoleUser.CONSOLE_UUID);
        setName("CONSOLE");
    }

    public ConsoleUser(final Map map) {
        super(map);
        setName("CONSOLE");
    }
}
