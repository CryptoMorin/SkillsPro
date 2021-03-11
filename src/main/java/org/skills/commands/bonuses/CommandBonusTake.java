package org.skills.commands.bonuses;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.events.SkillsEventType;
import org.skills.main.locale.SkillsLang;

public class CommandBonusTake extends SkillsCommand {
    public CommandBonusTake(SkillsCommand group) {
        super("take", group, SkillsLang.COMMAND_BONUS_TAKE_DESCRIPTION, "remove", "delete");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            OfflinePlayer p = getPlayer(sender, args[0]);
            if (p == null) return;

            SkillsEventType type = SkillsEventType.fromString(args[1]);
            if (type == null) {
                SkillsLang.COMMAND_BONUS_NOT_FOUND.sendMessage(sender, "%bonus%", args[1]);
                return;
            }

            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(p);
            info.removeBonus(type);
            SkillsLang.COMMAND_BONUS_TAKE_SUCCESS.sendMessage(sender);
        } else {
            SkillsCommandHandler.sendUsage(sender, "take <player> <xp/soul>");
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length > 2) return new String[0];
        if (args.length == 2) return new String[]{"soul", "XP"};
        return null;
    }
}
