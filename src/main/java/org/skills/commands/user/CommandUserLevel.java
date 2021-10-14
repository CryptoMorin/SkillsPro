package org.skills.commands.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.PlayerDataManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.LevelUp;
import org.skills.types.SkillScaling;

public class CommandUserLevel extends SkillsCommand {
    public CommandUserLevel(SkillsCommand group) {
        super("level", group, SkillsLang.COMMAND_USER_LEVEL_DESCRIPTION, false, "lvl", "lvls", "levels");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        CommandUser.handle(this, sender, args, (changeFactory, player, info, silent) -> {
            int pre = info.getLevel();
            int finalAmount = (int) changeFactory.withInitialAmount(pre).getFinalAmount();
            int maxLvl = (int) info.getScaling(SkillScaling.MAX_LEVEL);

            if (finalAmount >= maxLvl) {
                finalAmount = maxLvl;
                if (!silent && player.isOnline()) SkillsLang.MAX_LEVEL.sendMessage((Player) player);
            }

            info.setLevel(finalAmount);
            if (info.getXP() > info.getLevelXP()) info.setAbsoluteXP(0);
            if (!silent && finalAmount > pre) handleLevelup(info, player);

            PlayerDataManager.addLevel(player, finalAmount);
            return true;
        });
    }

    public void handleLevelup(SkilledPlayer info, OfflinePlayer player) {
        int currentLvl = info.getLevel();
        LevelUp level = null;

        for (int eachLvl = 0; eachLvl < currentLvl; eachLvl++) {
            LevelUp currentLevel = LevelUp.getProperties(eachLvl).forPlayer(player).evaluateRewards();
            if (level == null) {
                level = currentLevel;
            } else {
                level.add(currentLevel);
            }
        }

        if (level != null && player.isOnline()) {
            level.level(currentLvl);
            if (SkillsConfig.LEVEL_CELEBRATION.getBoolean()) level.celebrate((Player) player, plugin);
            level.performRewards().performMessages();
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return AmountChangeFactory.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<amount>", "[silent]"};
        if (args.length == 4) return new String[]{"silent"};
        return new String[0];
    }
}
