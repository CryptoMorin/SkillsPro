package org.skills.commands.user;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.SkillsLang;

public class CommandUserSouls extends SkillsCommand {
    public CommandUserSouls(SkillsCommand group) {
        super("souls", group, SkillsLang.COMMAND_USER_SOULS_DESCRIPTION, false, "soul");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        CommandUser.handle(this, sender, args, (changeFactory, player, info, silent) -> {
            info.setSouls((long) changeFactory.withInitialAmount(info.getSouls()).getFinalAmount());
            return true;
        });
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return AmountChangeFactory.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<amount>"};
        return new String[0];
    }
}
