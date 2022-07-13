package org.skills.main;

import org.skills.main.locale.MessageHandler;

import java.util.Arrays;
import java.util.function.Supplier;

public class SLogger {
    @SuppressWarnings("StringBufferField") private final StringBuilder builder;
    private boolean shown;

    public SLogger() {
        if (SkillsConfig.DEBUG.getBoolean()) {
            this.builder = new StringBuilder(100);
            //noinspection OptionalGetWithoutIsPresent
            this.builder
                    .append("---------------- ")
                    .append(Arrays.stream(Thread.currentThread().getStackTrace()).skip(2).findFirst().get())
                    .append(" ----------------")
                    .append('\n');
        } else {
            builder = null;
        }
    }

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

    public SLogger add(Supplier<String> str) {
        if (builder != null) builder.append(str.get()).append('\n');
        return this;
    }

    public SLogger add(String str) {
        if (builder != null) builder.append(str).append('\n');
        return this;
    }

    public void show() {
        if (shown) return;
        if (builder != null) {
            shown = true;
            this.builder.append("============================================");
            debug(builder.toString());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        show();
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
