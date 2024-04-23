package org.skills.commands.bonuses;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;
import org.skills.main.locale.SkillsLang;

public class CommandBonus extends SkillsCommand {
    public CommandBonus() {
        super("bonus", SkillsLang.COMMAND_BONUS_GIVE_DESCRIPTION, true, "bonuses");

        new CommandBonusGive(this);
        new CommandBonusTake(this);
        // new CommandBonusTime(this);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        SkillsCommandHandler.executeHelperForGroup(this, sender);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return args.length < 2 ? TabCompleteManager.getSubCommand(sender, this, args).toArray(new String[0]) : new String[0];
    }
}
