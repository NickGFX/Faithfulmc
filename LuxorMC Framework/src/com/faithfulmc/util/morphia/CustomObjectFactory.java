package com.faithfulmc.util.morphia;

import org.mongodb.morphia.mapping.DefaultCreator;

import java.lang.reflect.Constructor;

public class CustomObjectFactory extends DefaultCreator {
    private final ClassLoader classLoader;

    public CustomObjectFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    protected ClassLoader getClassLoaderForClass() {
        return classLoader;
    }

    private <T> T newInstance(Constructor<T> tryMe, Class<T> fallbackType) {
        if (tryMe != null) {
            tryMe.setAccessible(true);

            try {
                return tryMe.newInstance(new Object[0]);
            } catch (Exception var4) {
                throw new RuntimeException(var4);
            }
        } else {
            return this.createInstance(fallbackType);
        }
    }
}
