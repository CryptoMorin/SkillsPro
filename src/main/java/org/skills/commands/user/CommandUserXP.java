package org.skills.commands.user;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.SkillsLang;

public class CommandUserXP extends SkillsCommand {
    public CommandUserXP(SkillsCommand group) {
        super("xp", group, SkillsLang.COMMAND_USER_XP_DESCRIPTION, false, "exp");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        CommandUser.handle(this, sender, args, (changeFactory, player, info, silent) -> {
            double pre = silent ? info.getRawXP() : info.getXP();
            int finalAmount = (int) changeFactory.withInitialAmount(pre).getFinalAmount();

            if (silent) info.setRawXP(finalAmount, false);
            else info.setXP(changeFactory.getType() == AmountChangeFactory.Type.REMOVE ? -changeFactory.getAmount() : finalAmount);
            return true;
        });
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return AmountChangeFactory.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<amount>"};
        if (args.length == 4) return new String[]{"<raw>"};
        return new String[0];
    }
}
