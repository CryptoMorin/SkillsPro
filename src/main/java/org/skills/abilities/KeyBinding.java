package org.skills.abilities;

import org.skills.KeyBindingException;
import org.skills.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum KeyBinding {
    RIGHT_CLICK('R'), LEFT_CLICK('L'), SNEAK('S'), WHILE_SNEAK('C'), DROP('Q'), SWITCH('F');
    public static final Map<Character, KeyBinding> SHORT_NAME_BINDINGS = new HashMap<>();

    static {
        for (KeyBinding binding : values()) SHORT_NAME_BINDINGS.put(binding.shortName, binding);
    }

    public final char shortName;

    KeyBinding(char shortName) {
        this.shortName = shortName;
    }

    public static KeyBinding getKeyBindingFromName(char c) {
        return SHORT_NAME_BINDINGS.get(c);
    }

    public static String toString(KeyBinding[] binding) {
        char[] builder = new char[binding.length];
        for (int i = 0; i < binding.length; i++) builder[i] = binding[i].shortName;
        return new String(builder);
    }

    public static KeyBinding[] parseBinding(String binding) {
        binding = StringUtils.deleteWhitespace(binding);
        KeyBinding[] bindings = new KeyBinding[binding.length()];
        boolean previousWhileSneak = false;

        for (int i = 0; i < bindings.length; i++) {
            KeyBinding keyBinding = KeyBinding.getKeyBindingFromName(Character.toUpperCase(binding.charAt(i)));
            if (keyBinding == null) throw new KeyBindingException(binding, i, KeyBindingException.Reason.UNKNOWN_KEY);
            if (previousWhileSneak && (keyBinding == WHILE_SNEAK || keyBinding == SNEAK)) throw new KeyBindingException(binding, i, KeyBindingException.Reason.BAD_WHILE_SNEAK);
            if (keyBinding == WHILE_SNEAK) previousWhileSneak = true;
            bindings[i] = keyBinding;
        }

        return bindings;
    }

    public static boolean isIllegalBinding(KeyBinding[] first, KeyBinding[] second) {
        int minLength = Math.min(first.length, second.length);

        for (int i = 0; i < minLength; i++) {
            if (first[i] != second[i]) return false;
        }

        return true;
    }
}
