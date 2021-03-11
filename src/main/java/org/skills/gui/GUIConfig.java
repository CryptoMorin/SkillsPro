package org.skills.gui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.skills.data.AdapterYamlConfig;
import org.skills.main.SLogger;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GUIConfig {
    private static final Map<String, AdapterYamlConfig> GUIS = new HashMap<>();
    private final SkillsPro plugin;
    private final String lang;

    public GUIConfig(SkillsPro plugin) {
        this.plugin = plugin;
        String langOpt = SkillsConfig.LANG.getString();
        if (SkillsPro.get().getResource("guis/" + langOpt) == null &&
                !new File(SkillsPro.get().getDataFolder(), "guis/" + langOpt).exists()) lang = "en";
        else lang = langOpt;
        GUIS.clear();

        registerGUI("selector");
        registerGUI("friends");
        registerGUI("party");
        registerGUI("parties");
        registerGUI("shop");
        registerGUI("stats");
        registerGUI("stats-reset-confirmation");
        registerGUI("abilities");
        registerGUI("party-member");
        registerGUI("masteries");
    }

    public static AdapterYamlConfig getAdapter(String name) {
        return GUIS.get(name);
    }

    public static FileConfiguration getGUI(String name) {
        AdapterYamlConfig adapter = getAdapter(name);
        return adapter == null ? null : adapter.getConfig();
    }

    public static void registerGUI(Plugin plugin, String name) {
        String path = "guis/" + SkillsConfig.LANG.getString() + '/' + name + ".yml";
        if (SkillsPro.class.getResource('/' + path) == null) path = "guis/en/" + name + ".yml";
        AdapterYamlConfig adapter = new AdapterYamlConfig(new File(plugin.getDataFolder(), path), path).register();
        GUIS.put(name, adapter);
    }

    public void registerGUI(String name) {
        SLogger.debug("Loading GUI&8: &9" + name);
        String path = "guis/" + lang + '/' + name + ".yml";
        if (SkillsPro.class.getResource('/' + path) == null) path = "guis/en/" + name + ".yml";
        AdapterYamlConfig adapter = new AdapterYamlConfig(new File(plugin.getDataFolder(), path), path).register();
        GUIS.put(name, adapter);
    }

    public void reload() {
        GUIS.forEach((k, v) -> v.reloadConfig());
    }
}
