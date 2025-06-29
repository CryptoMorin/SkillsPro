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
 * A string util class extending Apache's {@code StringUtils}
 *
 * @author Crypto Morin
 * @version 3.0.0
 */
public final class StringUtils {
    public static final int INDEX_NOT_FOUND = -1;
    private static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###.##");

    /**
     * Capitalizes the first letter of a string and lowercase the other letters.
     * Apache's {@code org.apache.commons.lang.StringUtils#capitalize(String)} doesn't lowercase the other half.
     *
     * @param str the string to capitalize.
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

    /**
     * Find the first index of any of a set of potential substrings.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A {@code null} or zero length search array will return {@code -1}.
     * A {@code null} search array entry will be ignored, but a search
     * array containing "" will return {@code 0} if {@code str} is not
     * null. This method uses {@link String#indexOf(String)} if possible.</p>
     *
     * <pre>
     * StringUtils.indexOfAny(null, *)                      = -1
     * StringUtils.indexOfAny(*, null)                      = -1
     * StringUtils.indexOfAny(*, [])                        = -1
     * StringUtils.indexOfAny("zzabyycdxx", ["ab", "cd"])   = 2
     * StringUtils.indexOfAny("zzabyycdxx", ["cd", "ab"])   = 2
     * StringUtils.indexOfAny("zzabyycdxx", ["mn", "op"])   = -1
     * StringUtils.indexOfAny("zzabyycdxx", ["zab", "aby"]) = 1
     * StringUtils.indexOfAny("zzabyycdxx", [""])           = 0
     * StringUtils.indexOfAny("", [""])                     = 0
     * StringUtils.indexOfAny("", ["a"])                    = -1
     * </pre>
     *
     * @param str        the CharSequence to check, may be null
     * @param searchStrs the CharSequences to search for, may be null
     * @return the first index of any of the searchStrs in str, -1 if no match
     * @since 3.0 Changed signature from indexOfAny(String, String[]) to indexOfAny(CharSequence, CharSequence...)
     */
    public static int indexOfAny(final CharSequence str, final CharSequence... searchStrs) {
        if (str == null || searchStrs == null) {
            return INDEX_NOT_FOUND;
        }

        // String's can't have a MAX_VALUEth index.
        int ret = Integer.MAX_VALUE;

        int tmp;
        for (final CharSequence search : searchStrs) {
            if (search == null) {
                continue;
            }
            tmp = indexOf(str, search, 0);
            if (tmp == INDEX_NOT_FOUND) {
                continue;
            }

            if (tmp < ret) {
                ret = tmp;
            }
        }

        return ret == Integer.MAX_VALUE ? INDEX_NOT_FOUND : ret;
    }

    static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        if (cs == null || searchChar == null) {
            return StringUtils.INDEX_NOT_FOUND;
        }
        if (cs instanceof String) {
            return ((String) cs).indexOf(searchChar.toString(), start);
        }
        if (cs instanceof StringBuilder) {
            return ((StringBuilder) cs).indexOf(searchChar.toString(), start);
        }
        if (cs instanceof StringBuffer) {
            return ((StringBuffer) cs).indexOf(searchChar.toString(), start);
        }
        return cs.toString().indexOf(searchChar.toString(), start);
    }

    /**
     * Counts how many times the char appears in the given string.
     *
     * <p>A {@code null} or empty ("") String input returns {@code 0}.</p>
     *
     * <pre>
     * StringUtils.countMatches(null, *)     = 0
     * StringUtils.countMatches("", *)       = 0
     * StringUtils.countMatches("abba", 0)   = 0
     * StringUtils.countMatches("abba", 'a') = 2
     * StringUtils.countMatches("abba", 'b') = 2
     * StringUtils.countMatches("abba", 'x') = 0
     * </pre>
     *
     * @param str the CharSequence to check, may be null
     * @param ch  the char to count
     * @return the number of occurrences, 0 if the CharSequence is {@code null}
     * @since 3.4
     */
    public static int countMatches(final CharSequence str, final char ch) {
        int count = 0;
        // We could also call str.toCharArray() for faster lookups but that would generate more garbage.
        for (int i = 0; i < str.length(); i++) {
            if (ch == str.charAt(i)) {
                count++;
            }
        }
        return count;
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
            else if (clazz.startsWith("co.aikar") || clazz.startsWith("io.papermc") || clazz.startsWith("com.destroystokyo"))
                color = "&d";
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
    public static String remove(@Nullable String str, char remove) {
        if (com.google.common.base.Strings.isNullOrEmpty(str)) return str;
        char[] chars = str.toCharArray();
        int pos = 0;

        for (char ch : chars) {
            if (ch != remove) chars[pos++] = ch;
        }

        // the final pos will be the "length" because of the final pos++
        return chars.length == pos ? str : new String(chars, 0, pos);
    }

    /**
     * <p>Removes all occurrences of a substring from within the source string.</p>
     *
     * <p>A <code>null</code> source string will return <code>null</code>.
     * An empty ("") source string will return the empty string.
     * A <code>null</code> remove string will return the source string.
     * An empty ("") remove string will return the source string.</p>
     *
     * <pre>
     * StringUtils.remove(null, *)        = null
     * StringUtils.remove("", *)          = ""
     * StringUtils.remove(*, null)        = *
     * StringUtils.remove(*, "")          = *
     * StringUtils.remove("queued", "ue") = "qd"
     * StringUtils.remove("queued", "zz") = "queued"
     * </pre>
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     * <code>null</code> if null String input
     * @since 2.1
     */
    public static String remove(String str, String remove) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(remove);
        return replace(str, remove, "", -1);
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

    public static String[] split(@Nonnull String str, char separatorChar) {
        return split(str, separatorChar, true).toArray(new String[0]);
    }

    @Nonnull
    public static List<String> split(@Nonnull String str, char separatorChar, boolean preserveAllTokens) {
        if (Strings.isNullOrEmpty(str))
            throw new IllegalArgumentException("Cannot split a null or empty string: " + str);
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

    /**
     * <p>Replaces all occurrences of a String within another String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     * </pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     * <code>null</code> if null String input
     * @see #replace(String text, String searchString, String replacement, int max)
     */
    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed,
     * <code>null</code> if null String input
     */
    public static String replace(String text, String searchString, String replacement, int max) {
        if (text.isEmpty() || searchString.isEmpty() || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = Math.max(increase, 0);
        increase *= (max < 0 ? 16 : (Math.min(max, 64)));

        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
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
     * @return the config option.
     * @since 1.0.0
     */
    public static @NonNull
    String getGroupedOption(@NonNull String option, int... grouped) {
        Objects.requireNonNull(option, "Enum option name cannot be null");

        option = org.kingdoms.utils.string.Strings.toLatinLowerCase(option);
        if (grouped.length == 0) return option.replace('_', '-');

        String[] split = splitArray(option, '_', false);
        if (split.length < grouped.length)
            throw new IllegalArgumentException("Groups cannot be greater than enum separators: " + option);

        boolean[] groups = new boolean[split.length];
        for (int groupedInt : grouped) groups[groupedInt - 1] = true;

        StringBuilder sb = new StringBuilder(option.length());
        for (int i = 0; i < split.length; i++) {
            sb.append(split[i]);
            if (groups[i]) sb.append('.');
            else sb.append('-');
        }

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String[] splitArray(String str, char separatorChar) {
        return splitArray(str, separatorChar, false);
    }

    public static String[] splitArray(String str, char separatorChar, boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
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
        return list.toArray(new String[0]);
    }

    /**
     * Checks if the given string is equal to any of the other strings.
     *
     * @param str     the original string.
     * @param strings the other strings to check.
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
     * @return true if the original string contains one of the other strings, otherwise false.
     * @since 1.0.0
     */
    public static boolean containsAny(@Nullable String str, @NonNull String... strings) {
        if (Strings.isNullOrEmpty(str) || strings == null) return false;
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
