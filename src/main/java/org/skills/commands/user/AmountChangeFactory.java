package org.skills.commands.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.skills.main.locale.SkillsLang;

import java.util.Arrays;
import java.util.Locale;

public final class AmountChangeFactory {
    private final Type type;
    private final double amount;
    private double initialAmount;

    public AmountChangeFactory(Type type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    public static AmountChangeFactory of(CommandSender sender, String[] args) {
        Type type = getType(args[1]);
        double amount;

        if (type == null) {
            SkillsLang.Command_User_Invalid_Setter.sendMessage(sender, "%setter%", args[1]);
            return null;
        }

        String amountStr = args[2];
        try {
            amount = Double.parseDouble(amountStr);
        } catch (IllegalArgumentException ex) {
            SkillsLang.NOT_NUMBER.sendMessage(sender, "%arg%", amountStr);
            return null;
        }

        if (amount < 0) {
            if (type == Type.ADD) type = Type.REMOVE;
            //else if (type == Type.REMOVE) type = Type.ADD; No one does this to mean "add" ...
            amount = -amount;
        }

        return new AmountChangeFactory(type, amount);
    }

    protected static Type getType(String arg) {
        switch (arg.toLowerCase(Locale.ENGLISH)) {
            case "add":
            case "increase":
                return Type.ADD;
            case "remove":
            case "decrease":
                return Type.REMOVE;
            case "set":
                return Type.SET;
            default:
                return null;
        }
    }

    protected static String[] tabComplete(String starts) {
        String[] suggestions = {"add", "decrease", "set"};
        if (starts.isEmpty()) return suggestions;
        String lowerCase = starts.toLowerCase(Locale.ENGLISH);
        return Arrays.stream(suggestions).filter(x -> x.startsWith(lowerCase)).toArray(String[]::new);
    }

    public double getAmount() {
        return amount;
    }

    public Type getType() {
        return type;
    }

    public AmountChangeFactory withInitialAmount(double initialAmount) {
        this.initialAmount = initialAmount;
        return this;
    }

    public double getFinalAmount() {
        switch (type) {
            case ADD:
                return initialAmount + amount;
            case REMOVE:
                return initialAmount - amount;
            case SET:
                return amount;
            default:
                throw new AssertionError("Unknown setter type: " + type);
        }
    }

    public void handleSuccess(CommandSender sender, String langPath, OfflinePlayer player) {
        SkillsLang lang = SkillsLang.valueOf(langPath + '_' + type.name());
        lang.sendMessage(sender, "%player%", player.getName(), "%amount%", amount, "%new%", getFinalAmount());
    }

    public enum Type {ADD, REMOVE, SET}
}
