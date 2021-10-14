package org.skills.main.locale;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.skills.main.SLogger;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.StringUtils;
import org.skills.utils.YamlAdapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.Locale;

public class LanguageManager extends YamlAdapter {
    private static final EnumMap<SkillsLang, String> LANGS = new EnumMap<>(SkillsLang.class);
    public static boolean created = false;

    public LanguageManager(SkillsPro plugin) {
        super(new File(plugin.getDataFolder(), StringUtils.remove(SkillsConfig.LANG.getString(), ".yml").toLowerCase(Locale.ENGLISH) + ".yml"));
        load();
    }

    public static String getMessage(SkillsLang lang) {
        return LANGS.get(lang);
    }

    public static String buildMessage(String message, OfflinePlayer player, Object... edits) {
        message = MessageHandler.replaceVariables(message, edits);
        if (player == null) return MessageHandler.colorize(message);
        message = ServiceHandler.translatePlaceholders(player, message);
        return MessageHandler.colorize(message);
    }

    public boolean isSet(String option) {
        return XMaterial.supports(13) ? getConfig().isSet(option) : getConfig().contains(option);
    }

    private boolean saveDefaultLang() {
        InputStream stream = SkillsPro.get().getResource("languages/" + this.getFile().getName());
        if (stream == null) return false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        String line;
        try (BufferedWriter writer = Files.newBufferedWriter(this.getFile().toPath(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
        return true;
    }

    public void load() {
        int i = 0;
        if (!this.getFile().exists()) {
            created = true;
            Configuration config = getConfig();

            if (saveDefaultLang()) {
                for (SkillsLang lang : SkillsLang.VALUES) {
                    String parsed = config.getString(lang.getPath());
                    if (parsed == null) {
                        config.set(lang.getPath(), lang.getDefaultValue());
                        parsed = lang.getDefaultValue();
                    }

                    LANGS.put(lang, parsed);
                }
            } else {
                for (SkillsLang lang : SkillsLang.VALUES) {
                    this.getConfig().set(lang.getPath(), lang.getDefaultValue());
                    LANGS.put(lang, lang.getDefaultValue());
                }
            }

            saveConfig();
            i = SkillsLang.values().length;
        } else {
            this.reloadConfig();
            boolean needsSaving = false;
            for (SkillsLang lang : SkillsLang.VALUES) {
                if (!isSet(lang.getPath())) {
                    SLogger.warn("Setting missing language option " + lang + "...");
                    this.getConfig().set(lang.getPath(), lang.getDefaultValue());
                    LANGS.put(lang, lang.getDefaultValue());
                    needsSaving = true;
                } else {
                    LANGS.put(lang, this.getConfig().getString(lang.getPath()));
                }
                i++;
            }

            if (needsSaving) this.saveConfig();
        }

        MessageHandler.sendConsolePluginMessage("&2Loaded a total of &6" + i + " &2language statements.");
    }
}
