package org.skills.main;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.skills.main.locale.MessageHandler;

import java.util.List;
import java.util.Set;

public class SkillsSkillConfig {
    private final FileConfiguration config;
    private final String option;
    private String dynamicOption;
    private String property;

    public SkillsSkillConfig(FileConfiguration config, String option) {
        this.config = config;
        this.option = option;
    }

    public String getOption() {
        return option;
    }

    public SkillsSkillConfig withOption(Object... edits) {
        dynamicOption = MessageHandler.replaceVariables(option, edits);
        return this;
    }

    public SkillsSkillConfig withProperty(String property) {
        this.property = (Strings.isNullOrEmpty(dynamicOption) ? option : dynamicOption) + '.' + property;
        return this;
    }

    public SkillsSkillConfig back() {
        this.property = this.property.substring(0, this.property.lastIndexOf('.') - 1);
        return this;
    }

    public String getDynamicOption() {
        return Strings.isNullOrEmpty(property) ? (Strings.isNullOrEmpty(dynamicOption) ? option : dynamicOption) : property;
    }

    public boolean isSet() {
        return XMaterial.isNewVersion() ? config.isSet(getDynamicOption()) : config.contains(getDynamicOption());
    }

    public String getString() {
        return config.getString(getDynamicOption());
    }

    public List<String> getStringList() {
        return config.getStringList(getDynamicOption());
    }

    public Set<String> getSectionSet() {
        return config.getConfigurationSection(getDynamicOption()).getKeys(false);
    }

    public boolean getBoolean() {
        return config.getBoolean(getDynamicOption());
    }

    public int getInt() {
        return config.getInt(getDynamicOption());
    }

    public double getDouble() {
        return config.getDouble(getDynamicOption());
    }

    public long getLong() {
        return config.getLong(getDynamicOption());
    }

    public ConfigurationSection getSection() {
        return config.getConfigurationSection(getDynamicOption());
    }
}
