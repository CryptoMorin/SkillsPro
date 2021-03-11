package org.skills.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;

import java.io.File;
import java.io.IOException;

public class AdapterYamlConfig {
    private final String resourcePath;
    private final File file;
    private YamlConfiguration config;

    public AdapterYamlConfig(File file, String resourcePath) {
        this.file = file;
        this.resourcePath = resourcePath;
        config = YamlConfiguration.loadConfiguration(file);
    }

    public AdapterYamlConfig(File file) {
        this(file, file.getName());
    }

    public AdapterYamlConfig register() {
        if (!file.exists()) {
            config.options().copyDefaults(true);
            saveDefaultConfig(true);
            reloadConfig();
        }
        return this;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException ex) {
            SLogger.error("Error while attempting to save configuration file " + file.getName() + ": " + ex.getMessage());
        }
    }

    public File getFile() {
        return file;
    }

    public void saveDefaultConfig(boolean replace) {
        SkillsPro.get().saveResource(resourcePath, replace);
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}
