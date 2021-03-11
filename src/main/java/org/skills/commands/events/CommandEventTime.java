package org.skills.commands.events;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

public class CommandEventTime extends SkillsCommand {
    public CommandEventTime(SkillsCommand group) {
        super("time", group, SkillsLang.COMMAND_EVENT_TIME_DESCRIPTION, false, "changetime", "timechange");
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
