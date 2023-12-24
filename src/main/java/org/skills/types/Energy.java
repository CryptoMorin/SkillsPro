package org.skills.types;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.skills.main.SkillsPro;

import java.util.*;
import java.util.stream.Collectors;

public class Energy {
    public static final List<Energy> ENERGY = new ArrayList<>();
    private final String node;
    private final String name;
    private final String symbol;
    private final String color;
    private final String soundNotEnough;
    private final String soundFull;
    private final Set<ChargingMethod> chargingMethods;
    private final List<String> elements;

    public Energy(String node, String name, String symbol, String color, Set<ChargingMethod> chargingMethods, String soundNotEnough, String soundFull, List<String> elements) {
        this.node = node;
        this.name = name;
        this.symbol = symbol;
        this.color = color;
        this.chargingMethods = chargingMethods;
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

            Set<ChargingMethod> chargingMethods = EnumSet.noneOf(ChargingMethod.class);
            List<String> chargingList = energySection.getStringList("charging");
            if (chargingList != null && !chargingList.isEmpty()) {
                chargingMethods.addAll(chargingList.stream().map(x -> Enums.getIfPresent(ChargingMethod.class, x))
                        .filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.toList()));
            } else {
                String charging = energySection.getString("charging");
                if (charging == null) charging = "AUTO";
                else charging = charging.toUpperCase(Locale.ENGLISH);

                chargingMethods.add(Enums.getIfPresent(ChargingMethod.class, charging).or(ChargingMethod.AUTO));
            }

            String soundFull = energySection.getString("sounds.full");
            String soundNotEnough = energySection.getString("sounds.not-enough");

            Energy energy = new Energy(energyStr, energySection.getString("name"), energySection.getString("symbol"), energySection.getString("color"),
                    chargingMethods,
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

    public Set<ChargingMethod> getChargingMethods() {
        return chargingMethods;
    }

    public boolean hasChargingMethod(ChargingMethod method) {
        return chargingMethods.contains(method);
    }

    public String getSoundNotEnough() {
        return soundNotEnough;
    }

    public String getSoundFull() {
        return soundFull;
    }

    public enum ChargingMethod {
        AUTO, AUTO_REVERSE, KILL, HIT, EAT, PAIN, AUTO_NO_DAMAGE, REDUCE_ON_DAMAGE;
    }
}
