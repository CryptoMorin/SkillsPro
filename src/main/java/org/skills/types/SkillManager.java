package org.skills.types;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.skills.abilities.Ability;
import org.skills.abilities.AbilityManager;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.YamlAdapter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SkillManager {
    private static final HashMap<String, Skill> SKILLS = new HashMap<>();

    public static void init(SkillsPro plugin) {
        Ability.reload();
        AbilityManager.registerAll();

        Path skills = plugin.getDataFolder().toPath().resolve("Skills");
        if (!Files.exists(skills)) {
            URI uri = null;
            try {
                uri = SkillsPro.class.getResource("/Skills").toURI();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            try {
                try (FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<>())) {
                    for (Path path : zipfs.getRootDirectories()) {
                        Files.list(path.resolve("/Skills"))
                                .forEach(skillPath -> {
                                    String name = skillPath.getFileName().toString();
                                    name = name.substring(0, name.length() - 4);
                                    MessageHandler.sendConsolePluginMessage("&3Generating class&8: &e" + name);

                                    String pathLocation = skillPath.toString().substring(1);
                                    YamlAdapter adapter = new YamlAdapter(new File(plugin.getDataFolder(), pathLocation), pathLocation).register();
                                    Skill skill = new Skill(name);
                                    skill.setAdapter(adapter);
                                    registerScalings(skill);
                                    register(skill);
                                    skill.getAbilities().forEach(Ability::start);
                                });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.list(skills).forEach(skillPath -> {
                    String name = skillPath.getFileName().toString();
                    name = name.substring(0, name.length() - 4);
                    MessageHandler.sendConsolePluginMessage("&3Setting up class&8: &e" + name);

                    String pathLocation = "/Skills/" + skillPath.getFileName();
                    YamlAdapter adapter = new YamlAdapter(skillPath.toFile(), pathLocation);
                    Skill skill = new Skill(name);
                    skill.setAdapter(adapter);
                    registerScalings(skill);
                    register(skill);
                    skill.getAbilities().forEach(Ability::start);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        registerNone();
    }

    public static void unregister(String skill) {
        SKILLS.remove(skill.toLowerCase(Locale.ENGLISH));
    }

    public static boolean isSkillRegistered(String skill) {
        return SKILLS.containsKey(skill.toLowerCase(Locale.ENGLISH));
    }

    public static Skill getSkill(String skill) {
        return SKILLS.get(skill.toLowerCase(Locale.ENGLISH));
    }

    public static void register(Skill skill) {
        Validate.notEmpty(skill.getName(), "Cannot register skill with null or empty name");
        SKILLS.put(skill.getName().toLowerCase(Locale.ENGLISH), skill);
    }

    public static HashMap<String, Skill> getSkills() {
        return SKILLS;
    }

    public static void registerScalings(Skill skill) {
        skill.getAdapter().loadDefaults();
        FileConfiguration config = skill.getAdapter().getConfig();

        skill.addScaling(SkillScaling.HEALTH, config.getString("health"));
        skill.addScaling(SkillScaling.MAX_HEALTH, config.getString("max-health"));
        skill.addScaling(SkillScaling.MAX_ENERGY, config.getString("max-energy"));
        skill.addScaling(SkillScaling.ENERGY_REGEN, config.getString("energy-regen"));
        skill.addScaling(SkillScaling.MAX_LEVEL, config.getString("max-level"));
        String damageCap = config.getString("damage-cap");
        skill.addScaling(SkillScaling.DAMAGE_CAP, damageCap == null || damageCap.isEmpty() ? "0" : damageCap);
        skill.addScaling(SkillScaling.REQUIRED_LEVEL, config.getString("required-level"));
        skill.addScaling(SkillScaling.COST, config.getString("cost"));
        skill.setDisplayName(MessageHandler.colorize(config.getString("name")));

        //skill.setDisplayName(SkillsLang.valueOf("SKILL_" + skill.getName().toUpperCase(Locale.ENGLISH) + "_NAME").parse());
        skill.setStats(config.getStringList("stats"));
        Energy energy = Energy.getEnergy(config.getString("energy"));
        if (energy == null) energy = Energy.ENERGY.get(0);
        skill.setEnergy(energy);

        Map<String, Ability> abilities = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("abilities");
        for (String ability : section.getKeys(false)) {
            ability = ability.replace('-', '_');
            if (ability.equalsIgnoreCase("passive")) ability = skill.getName().toLowerCase(Locale.ENGLISH) + "_passive";

            Ability ab = AbilityManager.getAbility(ability);
            if (ab == null) {
                MessageHandler.sendConsolePluginMessage("&cCould not find ability named &e" + ability + " &cto register for &e" + skill.getName());
                continue;
            }

            abilities.put(ability, ab);
        }
        skill.setAbilities(abilities);
    }

    private static void registerNone() {
        MessageHandler.sendConsolePluginMessage("&3Setting up Skill&8: &eNone (Default)");
        Skill none = new Skill("none");
        none.addScaling(SkillScaling.MAX_LEVEL, SkillsConfig.DEFAULT_MAX_LEVEL.getString());
        none.setDisplayName(SkillsLang.NO_SKILL_DISPLAYNAME.parse());
        register(none);
    }
}
