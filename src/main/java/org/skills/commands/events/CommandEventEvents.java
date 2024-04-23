package org.skills.commands.events;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.commands.SkillsCommand;
import org.skills.events.SkillsBonus;
import org.skills.events.SkillsEventManager;
import org.skills.events.SkillsEventType;
import org.skills.main.locale.SkillsLang;

public class CommandEventEvents extends SkillsCommand {
    public CommandEventEvents(SkillsCommand group) {
        super("events", group, SkillsLang.COMMAND_EVENT_EVENTS_DESCRIPTION, false, "running");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        SkillsBonus xp = SkillsEventManager.getEvent(SkillsEventType.XP);
        SkillsBonus soul = SkillsEventManager.getEvent(SkillsEventType.SOUL);

        if (xp == null && soul == null) {
            SkillsLang.COMMAND_EVENT_EVENTS_NOTHING.sendMessage(sender);
            return;
        }

        if (xp != null)
            SkillsLang.COMMAND_EVENT_EVENTS_XP.sendMessage(sender, "%xp_duration%", xp.getDisplayDuration(), "%xp_multiplier%", xp.getMultiplier());
        if (soul != null)
            SkillsLang.COMMAND_EVENT_EVENTS_SOUL.sendMessage(sender, "%soul_duration%", soul.getDisplayDuration(), "%soul_multiplier%", soul.getMultiplier());
    }

    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[0];
    }
}