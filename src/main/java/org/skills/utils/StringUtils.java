/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.skills.utils;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.skills.main.locale.LanguageManager;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * A string util class extending Apache's {@link org.apache.commons.lang.StringUtils}
 *
 * @author Crypto Morin
 * @version 3.0.0
 */
public final class StringUtils extends org.apache.commons.lang.StringUtils {
    private static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###.##");

    /**
     * Capitalizes the first letter of a string and lowercase the other letters.
     * Apache's {@link org.apache.commons.lang.StringUtils#capitalize(String)} doesn't lowercase the other half.
     *
     * @param str the string to capitalize.
     *
     * @return a capitalized word.
     * @since 1.0.0
     */
    @Nullable
    public static String capitalize(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return str;
        int len = str.length();
        StringBuilder builder = new StringBuilder(len);
        boolean capitalizeNext = true;

        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch == ' ' || ch == '_') {
                if (ch != '_') builder.append(' ');
                capitalizeNext = true;
            } else if (capitalizeNext) {
                builder.append(Character.toTitleCase(ch));
                capitalizeNext = false;
            } else {
                // ch = (char) (ch | 0x20); // 32
                builder.append(Character.toLowerCase(ch));
            }
        }
        return builder.toString();
    }

    public static String configOption(@Nullable Enum<?> enumeral) {
        return configOption(enumeral.name());
    }

    public static String configOption(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return str;
        char[] chars = str.toCharArray();
        int len = str.length();

        for (int i = 0; i < len; i++) {
            char ch = chars[i];
            if (ch == '_') chars[i] = '-';
            else chars[i] = ((char) (ch | 0x20));
        }
        return new String(chars);
    }

    public static String toLatinLowerCase(@Nullable String str, char replace, char with) {
        if (Strings.isNullOrEmpty(str)) return str;
        char[] chars = str.toCharArray();
        int len = str.length();

        for (int i = 0; i < len; i++) {
            char ch = chars[i];
            if (ch == replace) chars[i] = with;
            else chars[i] = ((char) (ch | 0x20));
        }
        return new String(chars);
    }

    /**
     * Checks if every single character in a string is English.
     * English characters consists of whitespaces, alphabets and numbers
     * Characters such as <code>!@#$%^&*()_+|</code> will not be accepted.
     *
     * @param str the string to check.
     *
     * @return true if the whole string is in English, otherwise false.
     * @see #containsNumber(String)
     * @since 1.0.0
     */
    public static boolean isEnglish(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return true;
        for (char chr : str.toCharArray()) {
            if (chr != '_' && chr != ' ' && !isEnglishLetterOrDigit(chr)) return false;
        }
        return true;
    }

    public static void printStackTrace() {
        MessageHandler.sendConsolePluginMessage("&f--------------------------------------------");
        Arrays.stream(Thread.currentThread().getStackTrace()).skip(2).forEach(stack -> {
            String color;
            String clazz = stack.getClassName();
            if (clazz.startsWith("net.minecraft")) color = "&6";
            else if (clazz.startsWith("org.bukkit")) color = "&d";
            else if (clazz.startsWith("co.aikar") || clazz.startsWith("io.papermc") || clazz.startsWith("com.destroystokyo")) color = "&d";
            else if (clazz.startsWith("java")) color = "&c";
            else color = "&2";

            MessageHandler.sendConsolePluginMessage(color + stack.getClassName() + "&8.&9" + stack.getMethodName() + "&8: &5" + stack.getLineNumber());
        });
        MessageHandler.sendConsolePluginMessage("&f--------------------------------------------");
    }

    public static boolean hasSymbol(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return false;
        for (char chr : str.toCharArray()) {
            if (chr != '_' && chr != ' ' && !Character.isLetterOrDigit(chr)) return true;
        }
        return false;
    }

    public static boolean isEnglishLetter(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    public static boolean isEnglishDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    @Nonnull
    public static List<String> cleanSplit(@Nonnull String str, char separator) {
        return split(deleteWhitespace(str), separator, false);
    }

    @Nullable
    public static String deleteWhitespace(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return str;
        int len = str.length();
        char[] chs = new char[len];
        int count = 0;

        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch != ' ') chs[count++] = ch;
        }

        if (count == len) return str;
        return new String(chs, 0, count);
    }

    @Nonnull
    public static List<String> split(@Nonnull String str, char separatorChar, boolean preserveAllTokens) {
        if (Strings.isNullOrEmpty(str)) throw new IllegalArgumentException("Cannot split a null or empty string: " + str);
        int len = str.length();

        List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false, lastMatch = false;

        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }

                // This is important, it should not be i++
                start = ++i;
                continue;
            }

            lastMatch = false;
            match = true;
            i++;
        }

        if (match || (preserveAllTokens && lastMatch)) {
            list.add(str.substring(start, i));
        }
        return list;
    }

    public static boolean isEnglishLetterOrDigit(char ch) {
        return isEnglishDigit(ch) || isEnglishLetter(ch);
    }

    /**
     * Checks if the given string contains a number.
     *
     * @param str the string to check the numbers.
     *
     * @return true if the string contains any number, otherwise false.
     * @see #isEnglish(String)
     * @since 1.0.0
     */
    public static boolean containsNumber(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return false;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (isEnglishDigit(ch)) return true;
        }
        return false;
    }

    public static boolean containsAnyLangNumber(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return false;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (Character.isDigit(ch)) return true;
        }
        return false;
    }

    public static boolean isNumeric(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return false;
        int len = str.length();

        int start;
        char first = str.charAt(0);
        if (len != 1 && first == '-' || first == '+') start = 1;
        else start = 0;

        for (int i = start; i < len; i++) {
            char ch = str.charAt(i);
            if (!isEnglishDigit(ch)) return false;
        }
        return true;
    }

    public static BossBar parseBossBarFromConfig(Player player, ConfigurationSection section) {
        String title = LanguageManager.buildMessage(section.getString("title"), player);
        BarColor color = Enums.getIfPresent(BarColor.class, section.getString("color").toUpperCase(Locale.ENGLISH)).orNull();
        BarStyle style = Enums.getIfPresent(BarStyle.class, section.getString("style").toUpperCase(Locale.ENGLISH)).orNull();

        List<BarFlag> flags = new ArrayList<>();
        for (String flagName : section.getStringList("flags")) {
            BarFlag flag = Enums.getIfPresent(BarFlag.class, flagName.toUpperCase(Locale.ENGLISH)).orNull();
            if (flag != null) flags.add(flag);
        }

        BossBar bar = Bukkit.createBossBar(title, color, style, flags.toArray(new BarFlag[0]));
        if (player != null) bar.addPlayer(player);
        return bar;
    }

    public static boolean isPureNumber(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return false;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (!isEnglishDigit(ch)) return false;
        }
        return true;
    }

    /**
     * Translates an enum to a config option with grouped
     * key sections using dots.
     * <p>
     * <b>Examples</b>
     * <p><blockquote><pre>
     *         getGroupedOption("HELLO_WORLD");
     *         // Output: hello-world
     *
     *         getGroupedOption("WHATS_UP_WORLD", 1);
     *         // Output: whats: up-world:
     *
     *         getGroupedOption("GOODBYE_CRUEL_WORLD", 1, 2);
     *         // Output: goodbye: cruel: world:
     *     </pre></blockquote>
     *
     * @param grouped the groups index (will be replaced with "_").
     *
     * @return the config option.
     * @since 1.0.0
     */
    public static @NonNull
    String getGroupedOption(@NonNull String option, int... grouped) {
        Objects.requireNonNull(option, "Enum option name cannot be null");

        String groupStr = option.toLowerCase(Locale.ENGLISH);
        if (grouped.length == 0) return groupStr.replace('_', '-');
        String[] split = split(groupStr, '_');
        Validate.isTrue(split.length >= grouped.length, "Groups cannot be greater than enum separators");

        List<Integer> groups = new ArrayList<>();
        for (int groupedInt : grouped) groups.add(groupedInt - 1);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            sb.append(split[i]);
            if (groups.contains(i)) sb.append('.');
            else sb.append('-');
        }

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }


    /**
     * Checks if the given string is equal to any of the other strings.
     *
     * @param str     the original string.
     * @param strings the other strings to check.
     *
     * @return true if the original string is equal to one of the other strings, otherwise false.
     * @since 1.0.0
     */
    public static boolean isOneOf(@Nullable String str, @NonNull String... strings) {
        if (Strings.isNullOrEmpty(str)) return false;
        return Arrays.asList(strings).contains(str);
    }

    @Nonnull
    public static String toFancyNumber(double number) {
        return NUMBER_FORMAT.format(number);
    }

    /**
     * Gets the current full time.
     *
     * @return current time with the format of <b>yyyy/MM/dd hh:mm:ss</b>
     * @since 1.0.0
     */
    public static @NonNull
    String getFullTime() {
        return LocalDateTime.now().format(FULL_DATE_FORMAT);
    }

    @Nonnull
    public static String getFullTime(long epoch) {
        return FULL_DATE_FORMAT.withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epoch));
    }

    /**
     * Gets the current time.
     *
     * @return current time with the format of <b>hh:mm:ss</b>
     * @since 1.0.0
     */
    public static @NonNull
    String getTime() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }

    public static @NonNull
    String getDate(long epoch) {
        return DATE_FORMAT.withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epoch));
    }

    /**
     * Checks if the given string contains any of the other strings.
     *
     * @param str     the original string
     * @param strings the other strings to check
     *
     * @return true if the original string contains one of the other strings, otherwise false.
     * @since 1.0.0
     */
    public static boolean containsAny(@Nullable String str, @NonNull String... strings) {
        if (Strings.isNullOrEmpty(str) || strings == null || strings.length == 0) return false;
        for (String string : strings) {
            if (str.contains(string)) return true;
        }
        return false;
    }

    /**
     * Converts all the arguments to a single string from the start index.
     * <p>
     * <b>Examples</b>
     * <p><blockquote><pre>
     *         String args = {"hello", "hi", "goodbye"};
     *         buildArguments(args, ", ", 0);
     *         // Output: hello, hi, goodbye
     *
     *         buildArguments(args, "", 1);
     *         // Output: higoodbye
     *     </pre></blockquote>
     *
     * @param args    the arguments to convert to string.
     * @param joinStr the string to put between each argument when converting to string.
     * @param from    the index where it should start converting from.
     *
     * @return a converted string.
     * @since 1.0.0
     */
    public static String buildArguments(@NonNull String[] args, @NonNull String joinStr, int from) {
        Objects.requireNonNull(args, "Cannot build arguments for null argument list");
        Validate.isTrue(from >= 0, "Start index should be at least 0: " + from);
        Validate.isTrue(from < args.length, "Start index cannot be equal or greater than arguments length: " + from);

        return String.join(joinStr, Arrays.stream(args).skip(from).toArray(String[]::new));
    }

    public static void performCommands(Player player, List<String> commands) {
        Objects.requireNonNull(player, "Cannot perform commands to a null player");
        if (commands == null) return;

        for (String command : commands) {
            int index = command.indexOf(':');
            if (index != -1) {
                String option = command.substring(0, index).toUpperCase(Locale.ENGLISH);
                command = command.substring(index + 1);

                CommandSender executor = option.equals("CONSOLE") ? Bukkit.getConsoleSender() : player;
                boolean OP = option.equals("OP");

                if (OP) executor.setOp(true);
                Bukkit.dispatchCommand(executor, ServiceHandler.translatePlaceholders(player, command));
                if (OP) executor.setOp(false);
            } else {
                player.performCommand(ServiceHandler.translatePlaceholders(player, command));
            }
        }
    }

    public static String buildArguments(@NonNull String[] args, @NonNull String joinStr) {
        return buildArguments(args, joinStr, 0);
    }

    public static String buildArguments(@NonNull String[] args, int from) {
        return buildArguments(args, " ", from);
    }

    public static String buildArguments(@NonNull String[] args) {
        return buildArguments(args, " ", 0);
    }
}
