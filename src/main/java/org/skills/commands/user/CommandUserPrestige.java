package org.skills.commands.user;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.SkillsLang;

public class CommandUserPrestige extends SkillsCommand {
    public CommandUserPrestige(SkillsCommand group) {
        super("prestige", group, SkillsLang.COMMAND_USER_PRESTIGE_DESCRIPTION, false, "prstge", "rebirth");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        CommandUser.handle(this, sender, args, (changeFactory, player, info, silent) -> {
            int maxPrestige = info.getMaxPrestige();
            int finalAmount = (int) changeFactory.withInitialAmount(info.getPrestige()).getFinalAmount();
            int clamped = Math.max(0, Math.min(maxPrestige, finalAmount));

            if (clamped != finalAmount) changeFactory.withFinalAmount(clamped);
            info.setPrestige(clamped);
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