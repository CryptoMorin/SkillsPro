package org.skills.commands.user;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.TabCompleteManager;
import org.skills.main.locale.SkillsLang;
import org.skills.masteries.managers.Mastery;
import org.skills.masteries.managers.MasteryManager;

public class CommandUserMastery extends SkillsCommand {
    public CommandUserMastery(SkillsCommand group) {
        super("mastery", group, SkillsLang.COMMAND_USER_MASTERY_DESCRIPTION, false, "masteries");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        CommandUser.handle(this, sender, args, "mastery", (changeFactory, player, info, type, silent) -> {
            Mastery mastery = MasteryManager.getMastery(type);
            if (mastery == null) {
                SkillsLang.MASTERY_NOT_FOUND.sendMessage(sender, "%mastery%", type);
                return false;
            }

            int request = (int) changeFactory.withInitialAmount(info.getMasteryLevel(mastery)).getFinalAmount();
            info.setMasteryLevel(mastery, request);
            return true;
        });
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return AmountChangeFactory.tabComplete(args[1]);
        if (args.length == 3) return TabCompleteManager.getMasteries(args[2]);
        if (args.length == 4) return new String[]{"<amount>"};
        return new String[0];
    }
}