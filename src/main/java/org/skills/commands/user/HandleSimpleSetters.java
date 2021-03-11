package org.skills.commands.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.skills.main.locale.SkillsLang;

import java.util.Arrays;

public class HandleSimpleSetters {
    private static final String[] expressions = {"add", "increase", "remove", "decrease", "set", "setraw"};

    protected static double eval(String[] args, double data, int amount) {
        String arg = args[1];
        double finale = data;

        if (arg.equalsIgnoreCase("add") || arg.equalsIgnoreCase("increase")) finale += amount;
        if (arg.equalsIgnoreCase("remove") || arg.equalsIgnoreCase("decrease")) finale -= amount;
        if (arg.equalsIgnoreCase("set") || arg.equalsIgnoreCase("setraw")) finale = amount;

        return finale;
    }

    protected static boolean isAnySetter(String arg) {
        return Arrays.stream(expressions).anyMatch(e -> e.equalsIgnoreCase(arg));
    }

    protected static String[] tabComplete(String starts) {
        String[] suggestions = {"add", "decrease", "set"};
        if (starts.isEmpty()) return suggestions;
        return Arrays.stream(suggestions).filter(x -> x.startsWith(starts.toLowerCase())).toArray(String[]::new);
    }

    protected static boolean handleInvalidSetter(CommandSender sender, String[] args) {
        if (!HandleSimpleSetters.isAnySetter(args[1])) {
            SkillsLang.Command_User_Invalid_Setter.sendMessage(sender, "%setter%", args[1]);
            return true;
        }
        return false;
    }

    protected static void handleSuccess(CommandSender sender, SkillsLang lang, OfflinePlayer player, double amount, double newAmount) {
        lang.sendMessage(sender, "%player%", player.getName(), "%amount%", amount, "%new%", newAmount);
    }
}
