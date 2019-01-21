package com.faithfulmc.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Config extends YamlConfiguration {
    private final String fileName;
    private final JavaPlugin plugin;

    public Config(final JavaPlugin plugin, final String fileName) {
        this(plugin, fileName, ".yml");
    }

    public Config(final JavaPlugin plugin, final String fileName, final String fileExtension) {
        this.plugin = plugin;
        this.fileName = fileName + (fileName.endsWith(fileExtension) ? "" : fileExtension);
        this.createFile();
    }

    public String getFileName() {
        return this.fileName;
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    private void createFile() {
        final File folder = this.plugin.getDataFolder();
        try {
            final File ex = new File(folder, this.fileName);
            if (!ex.exists()) {
                if (this.plugin.getResource(this.fileName) != null) {
                    this.plugin.saveResource(this.fileName, false);
                } else {
                    this.save(ex);
                }
            } else {
                this.load(ex);
                this.save(ex);
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public void save() {
        final File folder = this.plugin.getDataFolder();
        try {
            this.save(new File(folder, this.fileName));
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Config)) {
            return false;
        }
        final Config config = (Config) o;
        if (this.fileName != null) {
            if (this.fileName.equals(config.fileName)) {
                return (this.plugin != null) ? this.plugin.equals((Object) config.plugin) : (config.plugin == null);
            }
        } else if (config.fileName == null) {
            return (this.plugin != null) ? this.plugin.equals((Object) config.plugin) : (config.plugin == null);
        }
        return false;
    }

    public int hashCode() {
        int result = (this.fileName != null) ? this.fileName.hashCode() : 0;
        result = 31 * result + ((this.plugin != null) ? this.plugin.hashCode() : 0);
        return result;
    }
}
