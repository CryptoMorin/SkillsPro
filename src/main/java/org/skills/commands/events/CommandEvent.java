package org.skills.commands.events;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;

public class CommandEvent extends SkillsCommand {
    public CommandEvent() {
        super("event", null, "", true, "events", "boost", "boosts");

        new CommandEventStart(this);
        new CommandEventStop(this);
        new CommandEventTime(this);
        new CommandEventEvents(this);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        SkillsCommandHandler.executeHelperForGroup(this, sender);
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return args.length < 2 ? TabCompleteManager.getSubCommand(sender, this, args).toArray(new String[0]) : new String[0];
    }
}
