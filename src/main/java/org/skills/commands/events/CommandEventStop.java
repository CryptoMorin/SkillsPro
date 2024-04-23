package org.skills.commands.events;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.events.SkillsBonus;
import org.skills.events.SkillsEventManager;
import org.skills.events.SkillsEventType;
import org.skills.main.locale.SkillsLang;

public class CommandEventStop extends SkillsCommand {
    public CommandEventStop(SkillsCommand group) {
        super("stop", group, SkillsLang.COMMAND_EVENT_STOP_DESCRIPTION, false);
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (args.length == 0) {
            SkillsCommandHandler.sendUsage(sender, "<xp/soul>");
            return;
        }
        SkillsEventType type = SkillsEventType.fromString(args[0]);
        if (type == null) {
            SkillsLang.COMMAND_EVENT_NOT_FOUND.sendMessage(sender, "%event%", args[0]);
            return;
        }

        SkillsBonus event = SkillsEventManager.getEvent(type);
        if (event == null) {
            SkillsLang.EVENT_NOT_RUNNING.sendMessage(sender, "%event%", type);
            return;
        }
        event.stop();
        SkillsLang.COMMAND_EVENT_STOP_SUCCESS.sendPlayersMessage("%player%", sender.getName(), "%event%", type);
    }

    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[]{"XP", "Soul"};
    }
}
