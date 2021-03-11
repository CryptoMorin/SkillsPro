package org.skills.types;

import org.apache.commons.lang.Validate;
import org.skills.abilities.Ability;
import org.skills.abilities.AbilityManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SLogger;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.MathUtils;
import org.skills.utils.StringUtils;
import org.skills.utils.YamlAdapter;

import java.util.*;

public class Skill {
    private final Map<SkillScaling, String> scaling = new EnumMap<>(SkillScaling.class);
    private final String name;
    private Map<String, Ability> abilities = new HashMap<>();
    private String displayName;
    private Energy energy;
    private List<Stat> stats;
    private YamlAdapter adapter;

    public Skill(String name) {
        this.name = name;
    }

    public void register() {
        Validate.notEmpty(name, "Skill name cannot be null or empty");
        SkillManager.register(this);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Skill)) return false;
        Skill skill = (Skill) obj;
        return this.name.equals(skill.name);
    }

    public boolean isNone() {
        return this.name.equals("none");
    }

    public boolean hasAbility(Ability ability) {
        return abilities.containsKey(ability.getName());
    }

    public void unregister() {
        SkillManager.unregister(this.name);
    }

    public double getScaling(SkilledPlayer info, SkillScaling type) {
        String scaling = this.scaling.get(type);
        if (scaling == null) {
            if (info.hasSkill()) throw new NullPointerException("Accessing null scaling: " + type);
            return 0;
        }

        String equation = StringUtils.replace(StringUtils.replace(
                ServiceHandler.translatePlaceholders(info.getOfflinePlayer(), scaling.toLowerCase(Locale.ENGLISH)),
                "lvl", String.valueOf(info.getLevel())),
                "bloodwell", String.valueOf(info.getImprovementLevel(AbilityManager.getAbility("blood_well"))));

        for (Stat stats : Stat.STATS.values()) {
            equation = MessageHandler.replace(equation, '%' + stats.getNode() + '%',
                    MessageHandler.Replacer.of(() -> String.valueOf(info.getStat(stats))));
        }

        return MathUtils.evaluateEquation(equation);
    }

    public void addScaling(SkillScaling type, String scale) {
        scaling.put(type, scale);
    }

    public String getName() {
        return name;
    }

    public List<Stat> getStats() {
        return stats;
    }

    public void setStats(List<String> stats) {
        List<Stat> types = new ArrayList<>(stats.size());
        for (String name : stats) {
            Stat stat = Stat.getStat(name.toLowerCase(Locale.ENGLISH));
            if (stat != null) types.add(stat);
            else SLogger.error("Unknown stat type '" + name + "' in skill: " + name + " (" + displayName + ')');
        }
        this.stats = types;
    }

    public Collection<Ability> getAbilities() {
        return abilities.values();
    }

    public void setAbilities(Map<String, Ability> abilities) {
        this.abilities = abilities;
    }

    public Ability getAbility(String abiltiy) {
        return abilities.get(abiltiy);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Energy getEnergy() {
        return energy;
    }

    public void setEnergy(Energy energy) {
        this.energy = energy;
    }

    public YamlAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(YamlAdapter adapter) {
        this.adapter = adapter;
    }
}