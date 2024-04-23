package org.skills.commands.bonuses;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.events.SkillsEventType;
import org.skills.events.SkillsPersonalBonus;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.MathEval;
import org.skills.utils.MathUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class CommandBonusGive extends SkillsCommand {
    public CommandBonusGive(SkillsCommand group) {
        super("give", group, SkillsLang.COMMAND_BONUS_GIVE_DESCRIPTION, "start");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 4) {
            try {
                OfflinePlayer p = getPlayer(sender, args[0]);
                if (p == null) return;

                SkillsEventType bonusType = SkillsEventType.fromString(args[1]);
                if (bonusType == null) {
                    SkillsLang.COMMAND_BONUS_NOT_FOUND.sendMessage(sender, "%bonus%", args[1]);
                    return;
                }

                Long time = MathUtils.calcMillis(args[2], TimeUnit.SECONDS);
                if (time == null) {
                    SkillsLang.INVALID_TIME.sendMessage(sender);
                    return;
                }

                String multiplier = args[3];
                if ((multiplier.contains("xp") && bonusType != SkillsEventType.XP) || (multiplier.contains("soul") && bonusType != SkillsEventType.SOUL)) {
                    SkillsLang.COMMAND_BONUS_GIVE_MULTIPLIER_ERROR.sendMessage(sender);
                    return;
                }
                try {
                    MathEval.evaluate(multiplier.replace("xp", "1").replace("soul", "1"));
                } catch (Throwable e) {
                    SkillsLang.COMMAND_BONUS_GIVE_MULTIPLIER_INVALID.sendMessage(sender);
                    return;
                }
                SkillsPersonalBonus bonus = new SkillsPersonalBonus(p.getUniqueId(), bonusType, multiplier,
                        Duration.ofMillis(time), System.currentTimeMillis());
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(p);
                info.addBonus(bonus);
                bonus.start();

                SkillsLang.COMMAND_BONUS_GIVE_CONFIRMATION.sendMessage(sender, "%player%", p.getName(), "%time%", args[2]);
                if (p.isOnline()) {
                    Player player = p.getPlayer();
                    SkillsLang.COMMAND_BONUS_GIVE_SUCCESS.sendMessage(player, "%time%", args[2]);
                }
            } catch (NumberFormatException e) {
                SkillsCommandHandler.sendUsage(sender, "<player> <xp/soul> <time> <multiplier>");
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "<player> <xp/soul> <time> <multiplier>");
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return new String[]{"soul", "XP"};
        String[] suggestions = {"<time>", "<multiplier>"};
        return TabCompleteManager.descendingSuggestions(suggestions, Arrays.stream(args).skip(2).toArray(String[]::new));
    }
}
