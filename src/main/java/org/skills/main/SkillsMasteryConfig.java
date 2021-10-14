package org.skills.main;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.skills.utils.StringUtils;
import org.skills.utils.YamlAdapter;

import java.util.List;

public enum SkillsMasteryConfig {
    MASTERIES_ENABLED("enabled"),
    DOWNGRADE,
    ENABLED("masteries.%.enabled"),
    NAME("masteries.%.name"),
    SCALING("masteries.%.scaling"),
    CHANCE("masteries.%.chance"),
    MAX_LEVEL("masteries.%.max-level"),
    REQUIRED_LEVEL("masteries.%.required-level"),
    COST("masteries.%.cost"),
    EXTRA;

    public static final YamlAdapter ADAPTER = new YamlAdapter(SkillsPro.get().getDataFolder().toPath().resolve("masteries.yml").toFile());
    private final String option;
    private String property;

    SkillsMasteryConfig(String option) {
        this.option = option;
        this.property = option;
    }

    SkillsMasteryConfig() {
        this.option = '.' + this.name().toLowerCase().replace('_', '-');
        this.property = option;
    }

    protected static YamlAdapter getAdapter() {
        return ADAPTER;
    }

    public boolean isSet() {
        return XMaterial.supports(13) ? ADAPTER.getConfig().isSet(this.option) : ADAPTER.getConfig().contains(this.option);
    }

    public String getOption() {
        return this.option;
    }

    public String getString() {
        return ADAPTER.getConfig().getString(this.property);
    }

    public SkillsMasteryConfig withProperties(String... properties) {
        if (this != EXTRA) property = StringUtils.replace(option, "%", properties[0].replace('_', '-'));
        else property = "masteries." + properties[0] + '.' + properties[1];
        return this;
    }

    public String getProperty() {
        return property;
    }

    public double getDouble() {
        return ADAPTER.getConfig().getDouble(this.property);
    }

    public Boolean getBoolean() {
        return ADAPTER.getConfig().getBoolean(this.property);
    }

    public ConfigurationSection getSection() {
        return ADAPTER.getConfig().getConfigurationSection(this.property);
    }

    public int getInt() {
        return ADAPTER.getConfig().getInt(this.property);
    }

    public List<String> getStringList() {
        return ADAPTER.getConfig().getStringList(this.property);
    }
}
