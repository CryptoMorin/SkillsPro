package org.skills.main;

import org.skills.main.locale.MessageHandler;

import java.util.function.Supplier;

public class SLogger {
    public static void info(String str) {
        SkillsPro.get().getLogger().info(str);
    }

    public static void debug(Supplier<String> strContainer) {
        if (SkillsConfig.DEBUG.getBoolean()) {
            String str = strContainer.get();
            MessageHandler.sendConsolePluginMessage("&7[&5DEBUG&7] &6" + str);
            MessageHandler.sendPlayersPluginMessage("&7[&5DEBUG&7] &6" + str);
        }
    }

    public static void debug(String str) {
        debug(() -> str);
    }

    public static void warn(String str) {
        SkillsPro.get().getLogger().warning(str);
    }

    public static void error(String str) {
        SkillsPro.get().getLogger().severe(str);
    }
}
