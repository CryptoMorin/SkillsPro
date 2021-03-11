package org.skills.commands.bonuses;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

public class CommandBonusTime extends SkillsCommand {
    public CommandBonusTime(SkillsCommand group) {
        super("time", group, SkillsLang.COMMAND_UNFRIEND_DESCRIPTION, "changetime", "timechange");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        MessageHandler.sendMessage(sender, "&cThis command is currently not available.");
    }


    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[0];
    }
}
