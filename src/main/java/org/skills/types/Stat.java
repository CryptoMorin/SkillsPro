package org.skills.types;

import com.google.common.base.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.skills.main.SkillsPro;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Stat {
    public static final Map<String, Stat> STATS = new HashMap<>();
    public static Stat POINTS;

    private final String node, upperNode;
    private final String name;
    private final String color;
    private final int maxLevel;

    public Stat(String node, String name, String color, int maxLevel) {
        this.node = node;
        this.upperNode = node.toUpperCase(Locale.ENGLISH);
        this.name = name;
        this.color = color;
        this.maxLevel = maxLevel;
    }

    public static void init(SkillsPro plugin) {
        STATS.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("stats.types");
        for (String stats : section.getKeys(false)) {
            ConfigurationSection statSection = section.getConfigurationSection(stats);

            stats = stats.toLowerCase(Locale.ENGLISH);
            Stat stat = new Stat(stats, statSection.getString("name"), statSection.getString("color"), statSection.getInt("max-level"));
            if (stats.equalsIgnoreCase("pts")) POINTS = stat;
            STATS.put(stats, stat);
        }
    }

    public static Stat getStat(String node) {
        return Strings.isNullOrEmpty(node) ? null : STATS.get(node);
    }

    public static boolean isPoints(String node) {
        return node.equals(POINTS.getDataNode());
    }

    public String getDataNode() {
        return upperNode;
    }

    public String getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public String getNode() {
        return node;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
