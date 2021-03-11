package org.skills.types;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.skills.main.SkillsPro;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Energy {
    public static final List<Energy> ENERGY = new ArrayList<>();
    private final String node;
    private final String name;
    private final String symbol;
    private final String color;
    private final String soundNotEnough;
    private final String soundFull;
    private final Charging charging;
    private final List<String> elements;

    public Energy(String node, String name, String symbol, String color, Charging charging, String soundNotEnough, String soundFull, List<String> elements) {
        this.node = node;
        this.name = name;
        this.symbol = symbol;
        this.color = color;
        this.charging = charging;
        this.elements = elements;
        this.soundNotEnough = soundNotEnough;
        this.soundFull = soundFull;
    }

    public static Energy getEnergy(String node) {
        if (Strings.isNullOrEmpty(node)) return null;
        return ENERGY.stream().filter(s -> s.node.equalsIgnoreCase(node)).findFirst().orElse(null);
    }

    public static void init(SkillsPro plugin) {
        ENERGY.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("energy");
        for (String energyStr : section.getKeys(false)) {
            ConfigurationSection energySection = section.getConfigurationSection(energyStr);
            String charging = energySection.getString("charging");
            if (charging == null) charging = "AUTO";
            else charging = charging.toUpperCase(Locale.ENGLISH);
            String soundFull = energySection.getString("sounds.full");
            String soundNotEnough = energySection.getString("sounds.not-enough");

            Energy energy = new Energy(energyStr, energySection.getString("name"), energySection.getString("symbol"), energySection.getString("color"),
                    Enums.getIfPresent(Charging.class, charging).or(Charging.AUTO),
                    soundNotEnough, soundFull,
                    energySection.getStringList("elements"));
            ENERGY.add(energy);
        }
    }

    public List<String> getElements() {
        return elements;
    }

    public String getColor() {
        return color;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public Charging getCharging() {
        return charging;
    }

    public String getSoundNotEnough() {
        return soundNotEnough;
    }

    public String getSoundFull() {
        return soundFull;
    }

    public enum Charging {
        AUTO, AUTO_REVERSE, KILL, HIT, EAT;
    }
}
