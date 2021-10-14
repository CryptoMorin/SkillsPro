package org.skills.types;

import org.bukkit.configuration.ConfigurationSection;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;
import org.skills.utils.StringUtils;

public enum StatType {
    DAMAGE, DEFENSE, CRITICAL_DAMAGE, CRITICAL_CHANCE, SPEED;

    private String equation;

    public static void init(SkillsPro plugin) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("stats.functional-types");
        for (StatType type : values()) {
            type.equation = section.getString(StringUtils.configOption(type));
        }
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public double evaluate(SkilledPlayer info) {
        return MathUtils.evaluateEquation(Stat.replaceStats(info, equation));
    }
}
