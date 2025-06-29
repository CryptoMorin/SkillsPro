package org.skills.main.locale;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.skills.utils.ArrayUtils;
import org.skills.utils.StringUtils;
import org.skills.utils.Validate;

import java.util.*;

public class HoverLang {
    public static void sendComplexMessage(CommandSender sender, OfflinePlayer placeholder, String str, Object... edits) {
        TextComponent comp = getComplexMessage(placeholder, str, edits);
        if (comp != null) sender.spigot().sendMessage(comp);
    }

    protected static TextComponent getComplexMessage(OfflinePlayer placeholder, String str, Object... edits) {
        if (!str.startsWith("COMPLEX:")) return null;

        Object[] complexEdits = null;
        if (edits != null && edits.length != 0) {
            Object first = edits[0];
            if (first instanceof Object[]) {
                complexEdits = (Object[]) first;
                edits = (Object[]) ArrayUtils.remove(edits, 0);
            }
        }

        str = str.substring(8);
        ComponentBuilder builder = new ComponentBuilder("");

        int index;
        while ((index = str.indexOf("hover:{")) != -1) {
            String beforeStr = str.substring(0, index);
            complexReplace(builder, LanguageManager.buildMessage(beforeStr, placeholder, edits), complexEdits);
            index += 7;

            int end = str.indexOf('}', index);
            String hover = str.substring(index, end);
            str = str.substring(end + 1);

            int sep = hover.indexOf(',');
            String msg = LanguageManager.buildMessage(hover.substring(0, sep), placeholder, edits);
            String hoverText = hover.substring(sep + 1);
            TextComponent textComp = new TextComponent(TextComponent.fromLegacyText(msg));

            int cmdIndex = hoverText.indexOf(',');
            if (cmdIndex != -1) {
                String action = hoverText.substring(cmdIndex + 1);
                hoverText = hoverText.substring(0, cmdIndex);

                ClickEvent.Action clickAction = ClickEvent.Action.RUN_COMMAND;
                if (action.startsWith("|")) {
                    action = action.substring(1);
                    clickAction = ClickEvent.Action.SUGGEST_COMMAND;
                } else if (action.startsWith("url:")) {
                    action = action.substring(4);
                    clickAction = ClickEvent.Action.OPEN_URL;
                }

                ClickEvent clickEvent = new ClickEvent(clickAction, LanguageManager.buildMessage(action, placeholder, edits));
                textComp.setClickEvent(clickEvent);
            }

            if (!hoverText.isEmpty()) {
                hoverText = LanguageManager.buildMessage(hoverText, placeholder, edits);
                TextComponent hoverComp = new TextComponent(TextComponent.fromLegacyText(hoverText));
                HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverComp});
                textComp.setHoverEvent(event);
            }

            builder.append(textComp);
        }

        complexReplace(builder, LanguageManager.buildMessage(str, placeholder, edits), complexEdits);
        return new TextComponent(builder.create());
    }

    private static void complexReplace(ComponentBuilder builder, String str, Object[] edits) {
        if (edits != null) {
            Map<String, TextComponent> replacements = new HashMap<>();
            List<String> vars = new ArrayList<>();
            for (int i = edits.length; i > 0; i -= 2) {
                String variable = String.valueOf(edits[i - 2]);
                vars.add(variable);
                TextComponent replacement = (TextComponent) edits[i - 1];
                replacements.put(variable, replacement);
            }

            int index;
            int last = 0;
            while ((index = StringUtils.indexOfAny(str, vars.toArray(new String[0]))) != -1) {
                String start = str.substring(index);
                TextComponent replacement = null;
                for (String variable : vars) {
                    if (start.startsWith(variable)) {
                        replacement = replacements.get(variable);
                        builder.append(new TextComponent(TextComponent.fromLegacyText(str.substring(last, index))), ComponentBuilder.FormatRetention.NONE);
                        str = str.substring(0, index) + str.substring(index + variable.length());
                        break;
                    }
                }
                Validate.isTrue(replacement != null, "Unexpected error. Cannot find reference to replacement of complex component: " + str + " START: " + start);
                builder.append(replacement);
                last = index;
            }
            if (last > 0)
                builder.append(new TextComponent(TextComponent.fromLegacyText(str.substring(last))), ComponentBuilder.FormatRetention.NONE);

//            for (int i = edits.length; i > 0; i -= 2) {
//                String variable = String.valueOf(edits[i - 2]);
//                TextComponent replacement = (TextComponent) edits[i - 1];
//
//                String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(str, variable);
//                for (int j=0; j<parts.length; j++) {
//                    if (j > 0) builder.append(replacement);
//                    builder.append(new TextComponent(TextComponent.fromLegacyText(parts[j])), ComponentBuilder.FormatRetention.NONE);
//                }
//            }
        } else {
            builder.append(new TextComponent(TextComponent.fromLegacyText(str)), ComponentBuilder.FormatRetention.NONE);
        }
    }

    public static class ComplexComponent {
        private final String original;
        private final boolean complex;
        private StringBuilder textBuilder;
        private ComponentBuilder componentBuilder;

        public ComplexComponent(String original) {
            this.original = original;
            complex = original.startsWith("COMPLEX:");
            if (complex) componentBuilder = new ComponentBuilder("");
            else textBuilder = new StringBuilder();
        }

        public static Object[] joinEdits(Object[]... edits) {
            List<Object> finalEdits = new ArrayList<>();
            for (Object[] edit : edits) finalEdits.addAll(Arrays.asList(edit));
            return finalEdits.toArray();
        }

        public static Object[] wrap(Object[] edits) {
            return new Object[]{edits};
        }

        public ComplexComponent append(OfflinePlayer placeholder, Object... edits) {
            return append(this.original, placeholder, edits);
        }

        public ComplexComponent append(String str, OfflinePlayer placeholder, Object... edits) {
            if (complex) {
                TextComponent component = getComplexMessage(placeholder, str, edits);
                componentBuilder.append(component);
            } else {
                String text = LanguageManager.buildMessage(str, placeholder, edits);
                textBuilder.append(text);
            }
            return this;
        }

        public ComplexComponent append(String str) {
            if (complex) {
                componentBuilder.append(new TextComponent(TextComponent.fromLegacyText(str)), ComponentBuilder.FormatRetention.NONE);
            } else {
                textBuilder.append(str);
            }
            return this;
        }

        public Object[] asComplexEdit(String varaible) {
            return complex ? new Object[]{varaible, new TextComponent(componentBuilder.create())} : new Object[]{varaible, textBuilder.toString()};
        }

        public Object[] asWrappedComplexEdit(String variable) {
            return wrap(asComplexEdit(variable));
        }
    }
}
