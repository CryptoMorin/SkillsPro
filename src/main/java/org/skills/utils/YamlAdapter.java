package org.skills.utils;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class YamlAdapter {
    private final Plugin plugin;
    private final String resourcePath;
    private final File file;
    private YamlConfiguration config;

    public YamlAdapter(Plugin plugin, File file, String resourcePath) {
        this.file = file;
        this.resourcePath = resourcePath;
        this.plugin = plugin;
        config = YamlConfiguration.loadConfiguration(file);
    }

    public YamlAdapter(File file, String resourcePath) {
        this(SkillsPro.get(), file, resourcePath);
    }

    public YamlAdapter(File file) {
        this(file, file.getName());
    }

    public YamlAdapter register() {
        if (!file.exists()) {
            config.options().copyDefaults(true);
            saveDefaultConfig(true);
            reloadConfig();
        }
        return this;
    }

    public void loadDefaults() {
        InputStream stream = plugin.getClass().getResourceAsStream(resourcePath);
        if (stream != null) config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, Charsets.UTF_8)));
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
        plugin.saveResource(resourcePath, replace);
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        register();
        reloadConfig();
    }
}
