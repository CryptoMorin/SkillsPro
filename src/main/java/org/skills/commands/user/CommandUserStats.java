package org.skills.commands.user;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.SkillsLang;
import org.skills.types.Stat;

public class CommandUserStats extends SkillsCommand {
    public CommandUserStats(SkillsCommand group) {
        super("stats", group, SkillsLang.COMMAND_USER_STATS_DESCRIPTION, false, "stat", "statpoints", "statspoint");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        CommandUser.handle(this, sender, args, (changeFactory, player, info, silent) -> {
            info.setStat(Stat.POINTS, (int) changeFactory.withInitialAmount(info.getPoints()).getFinalAmount());
            return true;
        });
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return AmountChangeFactory.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<amount>"};
        return new String[0];
    }
}