/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Crypto Morin
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
package org.skills.main.locale;

import com.google.common.base.Strings;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skills.utils.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Simplified yet heavily optimized message supplier services for colored messages
 * for players and console. And the ability to break lines simply by using <code>\n</code><br>
 * All the parameters should be non-null.
 *
 * @author Crypto Morin
 * @version 4.1.0
 */
public class MessageHandler {
    private static final boolean SIXTEEN;
    private static final char COLOR_CODE = '&';

    static {
        boolean sixteen;
        try {
            Class.forName("org.bukkit.entity.Zoglin");
            sixteen = true;
        } catch (ClassNotFoundException ignored) {
            sixteen = false;
        }
        SIXTEEN = sixteen;
    }

    /**
     * A simple method to simplify the process of replacing multiple strings
     * for a single string.<br>
     * Should be used in your locale manager, not here.
     * <p>
     * <b>Example</b>
     * <p><blockquote>
     * String test = ...;<br>
     * test = replaceAllLocalHolders(test, "%toaster%", ToasterUtils.getName(), "%capacity%", ToasterUtils.getCapacity());
     * </blockquote>
     *
     * @param str   the original string that is going to be checked.
     * @param edits the objects with their variables that are going to be replaced.
     *
     * @return a translated string with all the variables replaced.
     * @since 1.0.0
     */
    public static String replaceVariables(String str, Object... edits) {
        if (edits == null || edits.length == 0 || Strings.isNullOrEmpty(str)) return str;
        Validate.isTrue(!(edits[0] instanceof Collection), "First element of edits is a collection (bad method use)");
        Validate.isTrue((edits.length & 1) == 0,
                "No replacement is specified for the last variable: \"" + edits[edits.length - 1] + "\" in \"" + str + '"' + " with edits: \"" + Arrays.toString(edits) + '"');

        for (int i = edits.length; i > 0; i -= 2) {
            String variable = String.valueOf(edits[i - 2]);
            Object replacement = edits[i - 1];
            str = replace(str, variable, replacement);
        }
        return str;
    }

    /**
     * A version of {@link #replaceVariables(String, Object...)} without needing to convert the list to an array.
     *
     * @param str   the string to replace.
     * @param edits the edits to apply to the string.
     *
     * @return a replaced string.
     * @since 3.0.0
     */
    public static String replaceVariables(String str, List<Object> edits) {
        if (edits == null || edits.isEmpty() || Strings.isNullOrEmpty(str)) return str;
        Validate.isTrue((edits.size() & 1) == 0,
                "No replacement is specified for the last variable: \"" + edits.get(edits.size() - 1) + "\" in \"" + str + '"' + " with edits: \"" + Arrays.toString(edits.toArray()) + '"');

        String variable = null;
        for (Object edit : edits) {
            if (variable == null) {
                variable = String.valueOf(edit);
            } else {
                str = replace(str, variable, edit);
                variable = null;
            }
        }
        return str;
    }

    /**
     * Replaces a variable in a string in the most efficient way possible that is
     * the most compatible with {@link #replaceVariables(String, Object...)} and {@link #replaceVariables(String, List)}
     * <p>
     * Also supports {@link Supplier} as its replacement.
     * Supllied replacements have the ability to not be evaluated if the
     * specified string doesn't contain the variable.
     *
     * @param text     the string to replace the variable in.
     * @param variable the variable to replace.
     * @param replace  the replacement object. A supplier in special cases.
     *
     * @return the string with the variable replaced.
     * @since 2.0.0
     */
    public static String replace(String text, String variable, Object replace) {
        int find = text.indexOf(variable);
        if (find == -1) return text;

        int start = 0;
        int len = text.length();
        int varLen = variable.length();
        StringBuilder builder = new StringBuilder(len);

        String replacement;
        if (replace instanceof Supplier) {
            Supplier<?> replacer = (Supplier<?>) replace;
            replacement = String.valueOf(replacer.get());
        } else {
            replacement = String.valueOf(replace);
        }

        while (find != -1) {
            builder.append(text, start, find).append(replacement);
            start = find + varLen;
            find = text.indexOf(variable, start);
        }

        builder.append(text, start, len);
        return builder.toString();
    }

    public static void sendMessage(CommandSender receiver, String message, boolean prefix) {
        Objects.requireNonNull(receiver, "Receiver/Sender cannot be null");
        if (Strings.isNullOrEmpty(message)) return;

        String prefixStr = prefix ? SkillsLang.PREFIX.parse() : "";
        String lastColors = "";
        List<String> split = StringUtils.split(message, '\n', true);
        int len = split.size();

        for (String msg : split) {
            msg = lastColors + colorize(msg);
            receiver.sendMessage(prefixStr + msg);
            if (--len != 0) lastColors = getLastColors(msg);
        }
    }

    /**
     * Gets the last colors of a string.
     * The last color is determined by a non-format color. Meaning all the format codes will
     * be included in the last color, but it'll stop once it found an actual (hex) color code.
     *
     * @param input the string to search for colors.
     *
     * @return translated last color of the string.
     * @since 3.0.0
     */
    public static String getLastColors(@NotNull String input) {
        StringBuilder builder = new StringBuilder();
        int length = input.length();
        int hexLength = (6 * 2) + 2;

        for (int index = length - 1; index > -1; --index) {
            if (input.charAt(index) == ChatColor.COLOR_CHAR) {
                char next = input.charAt(index + 1);

                if (SIXTEEN) {
                    if (index >= 12 && input.charAt(index - 12) == ChatColor.COLOR_CHAR && input.charAt(index - 11) == 'x') {
                        StringBuilder hexIdentifier = new StringBuilder(hexLength);
                        hexIdentifier.append(ChatColor.COLOR_CHAR).append('x');

                        index -= 10;
                        for (int j = index; j < index + 12; j += 2) {
                            if (input.charAt(j) == ChatColor.COLOR_CHAR) {
                                char hex = input.charAt(j + 1);
                                if (isHexOrDigit(hex)) hexIdentifier.append(ChatColor.COLOR_CHAR).append(hex);
                                else break;
                            } else break;
                        }

                        if (hexIdentifier.length() == hexLength) {
                            builder.insert(0, hexIdentifier);
                            break;
                        } else {
                            String scanner = hexIdentifier.substring(2);
                            int scannerLen = scanner.length();
                            if (scanner.isEmpty()) continue;

                            for (int indexJ = scannerLen - 1; indexJ > -1; indexJ--) {
                                if (scanner.charAt(indexJ) == ChatColor.COLOR_CHAR) {
                                    char colorJ = scanner.charAt(index + 1);
                                    if (appendColorBuilder(builder, colorJ)) return builder.toString();
                                }
                            }
                        }
                    } else {
                        if (appendColorBuilder(builder, next)) break;
                    }
                } else {
                    if (appendColorBuilder(builder, next)) break;
                }
            }
        }

        return builder.toString();
    }

    /**
     * Appends the last color code to the color code builder.
     *
     * @param builder the color code builder.
     * @param code    the color code.
     *
     * @return if this color code was a formatted color code.
     * @since 3.0.0
     */
    private static boolean appendColorBuilder(StringBuilder builder, char code) {
        boolean isFormat = isFormattingCode(code);
        if (isFormat || isHexOrDigit(code)) {
            builder.insert(0, code).insert(0, ChatColor.COLOR_CHAR);
            if (isFormat) {
                if ((code == 'R' || code == 'r')) {
                    builder.setLength(0);
                    return true;
                }
            } else return true;
        }
        return false;
    }

    /**
     * Translates color codes for the given string using <code>&</code> character.
     *
     * @param str the string to translate.
     *
     * @return Translated Alternate Color Codes of the string.
     * @since 1.0.0
     */
    public static String colorize(String str) {
        if (str.isEmpty()) return str;
        int len = str.length() - 1;

        if (SIXTEEN) {
            StringBuilder builder = new StringBuilder(len + 50);
            int hexState = -1;
            for (int i = 0; i < len; i++) {
                char current = str.charAt(i);

                if (hexState >= 0) {
                    if (isHexOrDigit(current)) {
                        builder.append(ChatColor.COLOR_CHAR).append(current);
                        if (hexState++ == 6) hexState = -1;
                    } else {
                        hexState = -1;
                        builder.append(current);
                    }
                } else {
                    if (current == COLOR_CODE) {
                        char next = str.charAt(i + 1);
                        boolean isHex = next == '#';

                        if (isHex || isColorCode(next)) {
                            builder.append(ChatColor.COLOR_CHAR);
                            if (isHex) {
                                builder.append('x');
                                hexState = 1;
                                i++;
                            }
                        } else builder.append(COLOR_CODE);
                    } else builder.append(current);
                }
            }

            if (hexState == 6) builder.append(ChatColor.COLOR_CHAR);
            builder.append(str.charAt(len));
            return builder.toString();
        } else {
            char[] chars = str.toCharArray();
            for (int i = 0; i < len; i++) {
                if (chars[i] == COLOR_CODE && isColorCode(chars[i + 1])) {
                    chars[i] = ChatColor.COLOR_CHAR;
                    i++;
                }
            }
            return new String(chars);
        }
    }

    /**
     * Checks if this character can be a color code.
     * A part of color codes can be {@link #isHexOrDigit(char)} hex or {@link #isFormattingCode(char)} color code.
     *
     * @param ch the character.
     *
     * @return true if this character can color code, otherwise false.
     * @since 3.0.0
     */
    private static boolean isColorCode(char ch) {
        return isHexOrDigit(ch) || isFormattingCode(ch);
    }

    /**
     * Checks if this character can be a special color code.
     * Special color codes are bolds, italics, resets, underlines, strikethrough and obfuscated.
     *
     * @param ch the character.
     *
     * @return true if this character can color code, otherwise false.
     * @since 3.0.0
     */
    private static boolean isFormattingCode(char ch) {
        return (ch >= 'K' && ch <= 'O') || (ch >= 'k' && ch <= 'o') || (ch == 'R' || ch == 'r');
    }

    /**
     * Checks if this character can be a part of hex color.
     * https://simple.wikipedia.org/wiki/Hexadecimal
     *
     * @param ch the character.
     *
     * @return true if this character can be a hex color code, otherwise false.
     * @since 3.0.0
     */
    private static boolean isHexOrDigit(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }

    /**
     * Strips all the color codes from the string and replacing the section character
     * with {@link #COLOR_CODE} for both hex colors and normal color codes.
     *
     * @param str   the string to strip colors from.
     * @param strip whether the colors should be stripped only or removed completely.
     *
     * @return a string with visible colors.
     * @since 3.0.0
     */
    public static String stripColors(String str, boolean strip) {
        int len = str.length();

        if (SIXTEEN) {
            StringBuilder builder = new StringBuilder(len);
            int hexState = -1;

            for (int i = 0; i < len - 1; i++) {
                char ch = str.charAt(i);
                if (ch == ChatColor.COLOR_CHAR) {
                    char next = str.charAt(i + 1);

                    if (hexState != -1) {
                        if (isHexOrDigit(next)) {
                            if (strip) builder.append(next);
                            if (hexState++ == 6) hexState = -1;
                            i++;
                        } else hexState = -1;
                    } else {
                        boolean isHex = next == 'x';
                        if (isHex || isColorCode(next)) {
                            if (strip) builder.append(COLOR_CODE);
                            if (isHex) {
                                if (strip) builder.append('#');
                                hexState = 0;
                            } else if (strip) {
                                builder.append(next);
                            }
                            i++;
                        }
                    }
                } else {
                    hexState = -1;
                    builder.append(ch);
                }
            }

            builder.append(str.charAt(len - 1));
            return builder.toString();
        } else {
            if (strip) {
                char[] chars = str.toCharArray();
                for (int i = 0; i < len - 1; i++) {
                    if (chars[i] == ChatColor.COLOR_CHAR && isColorCode(chars[i + 1])) {
                        chars[i] = COLOR_CODE;
                        i++;
                    }
                }
                return new String(chars);
            } else {
                char[] chars = new char[len];
                int count = 0;
                for (int i = 0; i < len - 1; i++) {
                    char ch = str.charAt(i);
                    if (ch == ChatColor.COLOR_CHAR && isColorCode(str.charAt(i + 1))) i++;
                    else chars[count++] = ch;
                }
                return new String(chars, 0, count);
            }
        }
    }

    /**
     * Sends a colored message to either console or the player depending on if
     * the receiver is a player or console.
     * Mostly useful for commnad handlers.
     *
     * @param receiver the sender who's going to receive the message.
     * @param message  the message to send to the receiver.
     *
     * @see #sendPluginMessage(CommandSender, String)
     * @since 1.0.0
     */
    public static void sendMessage(CommandSender receiver, String message) {
        if (receiver instanceof Player) sendPlayerMessage((Player) receiver, message);
        else sendConsoleMessage(message);
    }

    /**
     * Sends a colored message with the plugin's prefix to either console or the player depending on if
     * the receiver is a player or console.
     *
     * @param receiver the sender who's going to receive the message.
     * @param message  the message to send to the receiver.
     *
     * @see #sendMessage(CommandSender, String)
     * @since 1.0.0
     */
    public static void sendPluginMessage(CommandSender receiver, String message) {
        if (receiver instanceof Player) sendPlayerPluginMessage((Player) receiver, message);
        else sendConsolePluginMessage(message);
    }

    /**
     * Sends a colored message to a player.
     *
     * @param player  the player who's going to receive the message.
     * @param message the message that is going to be sent to the player.
     *
     * @since 1.0.0
     */
    public static void sendPlayerMessage(Player player, String message) {
        sendMessage(player, message, false);
    }

    /**
     * Sends a colored message with the plugin's prefix to a player.
     *
     * @param player  the player who's going to receive the message.
     * @param message the message that is going to be sent to the player.
     *
     * @since 1.0.0
     */
    public static void sendPlayerPluginMessage(Player player, String message) {
        sendMessage(player, message, true);
    }

    /**
     * Sends a colored message to console.
     *
     * @param message the message that is going to be sent to console.
     *
     * @since 1.0.0
     */
    public static void sendConsoleMessage(String message) {
        sendMessage(Bukkit.getConsoleSender(), message, false);
    }

    /**
     * Sends a colored message using the plugin's prefix to console.
     *
     * @param message the message that is going to be sent to console.
     *
     * @since 1.0.0
     */
    public static void sendConsolePluginMessage(String message) {
        sendMessage(Bukkit.getConsoleSender(), message, true);
    }

    /**
     * Sends a colored message to all the online players on the server.
     *
     * @param message the message that is going to be sent to the players.
     *
     * @since 1.0.0
     */
    public static void sendPlayersMessage(String message) {
        for (Player players : Bukkit.getOnlinePlayers()) sendMessage(players, message, false);
    }

    /**
     * Sends a colored message with the plugin's prefix to all the online players on the server.
     *
     * @param message the message that is going to be sent to the players.
     *
     * @since 1.0.0
     */
    public static void sendPlayersPluginMessage(String message) {
        for (Player players : Bukkit.getOnlinePlayers()) sendMessage(players, message, true);
    }

    /**
     * Sends a debug message to console and all the online players with the
     * "pluginName.debug" permission. You need to replace the permission name inside the method yourself.
     * You can also change the debug prefix yourself inside the method.
     *
     * @param message the message to send to console and the players.
     *
     * @since 1.0.0
     */
    public static void debug(String message) {
        String msg = "&8[&5DEBUG&8] &4" + message;
        sendMessage(Bukkit.getConsoleSender(), msg, true);
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (players.hasPermission("kingdoms.debug")) sendMessage(players, msg, true);
        }
    }
}