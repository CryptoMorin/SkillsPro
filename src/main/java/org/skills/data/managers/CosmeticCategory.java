package org.skills.data.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.skills.gui.GUIConfig;
import org.skills.main.SkillsPro;

import java.util.HashMap;
import java.util.Map;

public class CosmeticCategory {
    protected static final Map<String, CosmeticCategory> CATEGORIES = new HashMap<>();
    private final String name;
    private final String command;
    private final String description;
    private Map<String, Cosmetic> cosmetics = new HashMap<>();

    public CosmeticCategory(String name, String command, String description) {
        this.name = name;
        this.command = command;
        this.description = description;
    }

    public static CosmeticCategory get(String category) {
        return CATEGORIES.get(category);
    }

    public static Map<String, CosmeticCategory> getCategories() {
        return CATEGORIES;
    }

    public static void load(SkillsPro plugin) {
        CATEGORIES.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("cosmetics");
        for (String categoryName : section.getKeys(false)) {
            ConfigurationSection categorySection = section.getConfigurationSection(categoryName);
            CosmeticCategory category = new CosmeticCategory(categoryName, categorySection.getString("command.name"), categorySection.getString("command.description"));
            ConfigurationSection elements = categorySection.getConfigurationSection("elements");

            for (String cosmeticName : elements.getKeys(false)) {
                if (cosmeticName.equals("command")) continue;
                ConfigurationSection cosmeticSection = elements.getConfigurationSection(cosmeticName);
                Cosmetic cosmetic = new Cosmetic(category, cosmeticName, cosmeticSection.getString("displayname"), cosmeticSection.getString("color"));
                category.cosmetics.put(cosmeticName, cosmetic);
            }

            CATEGORIES.put(categoryName, category);
            GUIConfig.registerGUI(plugin, categoryName);
        }
    }

    public String getDescription() {
        return description;
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public Map<String, Cosmetic> getCosmetics() {
        return cosmetics;
    }

    public void setCosmetics(Map<String, Cosmetic> cosmetics) {
        this.cosmetics = cosmetics;
    }

    public Cosmetic getCosmetic(String cosmetic) {
        return cosmetics.get(cosmetic);
    }
}
