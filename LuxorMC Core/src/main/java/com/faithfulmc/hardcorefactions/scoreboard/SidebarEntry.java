package com.faithfulmc.hardcorefactions.scoreboard;

public class SidebarEntry {
    public final String name;
    public String prefix;
    public String suffix;

    public SidebarEntry(String name) {
        this.name = name;
    }

    public SidebarEntry(Object name) {
        this.name = String.valueOf(name);
    }

    public SidebarEntry(String prefix, String name, String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public SidebarEntry(Object prefix, Object name, Object suffix) {
        this(name);
        this.prefix = String.valueOf(prefix);
        this.suffix = String.valueOf(suffix);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SidebarEntry)) {
            return false;
        }
        SidebarEntry that = (SidebarEntry) o;
        if (this.name != null ? !this.name.equals(that.name) :

                that.name != null) {
            return false;
        }
        if (this.prefix != null ? !this.prefix.equals(that.prefix) :

                that.prefix != null) {
            return false;
        }
        if (this.suffix != null) {
            if (!this.suffix.equals(that.suffix)) {
                return false;
            }
        } else if (that.suffix != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = this.name != null ? this.name.hashCode() : 0;
        result = 31 * result + (this.prefix != null ? this.prefix.hashCode() : 0);
        result = 31 * result + (this.suffix != null ? this.suffix.hashCode() : 0);
        return result;
    }
}
