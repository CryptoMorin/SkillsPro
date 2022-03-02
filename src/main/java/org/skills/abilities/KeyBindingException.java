package org.skills.abilities;

import com.google.common.base.Strings;

public class KeyBindingException extends RuntimeException {
    private final String binding;
    private final int index;
    private final Reason reason;

    public KeyBindingException(String binding, int index, Reason reason) {
        super(constructMessage(binding, index, reason));
        this.binding = binding;
        this.index = index;
        this.reason = reason;
    }

    private static String constructMessage(String binding, int index, Reason reason) {
        String msg = reason + " '" + binding.charAt(index) + "' at index " + index + " in binding: " + binding;
        return msg + ('\n' + Strings.repeat(" ", (msg.length() - binding.length()) + index));
    }

    public String getBinding() {
        return binding;
    }

    public int getIndex() {
        return index;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        UNKNOWN_KEY, BAD_WHILE_SNEAK;
    }
}
