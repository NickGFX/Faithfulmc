package com.faithfulmc.framework.warp;

import com.faithfulmc.util.PersistableLocation;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

public class Warp extends PersistableLocation implements ConfigurationSerializable {
    private String name;
    private String permission;

    public Warp(final String name, final Location location) {
        super(location);
        Preconditions.checkNotNull((Object) name, (Object) "Warp name cannot be null");
        Preconditions.checkNotNull((Object) location, (Object) "Warp location cannot be null");
        this.name = name;
        this.permission = "warp." + name;
    }

    public Warp(final Map<String, Object> map) {
        super(map);
        this.name = (String) map.get("name");
        this.permission = (String) map.get("permission");
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = (Map<String, Object>) super.serialize();
        map.put("name", this.name);
        map.put("permission", this.permission);
        return map;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        Preconditions.checkNotNull((Object) name, (Object) "Warp name cannot be null");
        this.name = name;
    }

    public String getPermission() {
        return this.permission;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Warp warp = (Warp) o;
        if (this.name != null) {
            if (!this.name.equals(warp.name)) {
                return false;
            }
        } else if (warp.name != null) {
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + ((this.name != null) ? this.name.hashCode() : 0);
        return result;
    }
}
