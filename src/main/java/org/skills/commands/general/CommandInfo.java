package org.skills.commands.general;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.events.SkillsBonus;
import org.skills.events.SkillsEventType;
import org.skills.events.SkillsPersonalBonus;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

import java.util.Collection;

public class CommandInfo extends SkillsCommand {
    public CommandInfo() {
        super("info", SkillsLang.COMMAND_INFO_DESCRIPTION, false, "see", "show", "level", "xp", "exp", "lvl", "levels", "lvls", "who");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 0) {
            if (!sender.hasPermission("skills.command.info.others")) {
                SkillsLang.COMMAND_INFO_OTHERS_PERMISSION.sendMessage(sender);
                return;
            }

            OfflinePlayer player = getPlayer(sender, args[0]);
            if (player == null) return;

            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
            SkillsLang lang = sender.hasPermission("skills.command.info.others.details") ? SkillsLang.COMMAND_INFO_MESSAGE : SkillsLang.COMMAND_INFO_OTHERS;

            lang.sendMessage(sender, player);
            String bonus = getBonusString(info);
            if (!bonus.isEmpty()) MessageHandler.sendMessage(sender, bonus);
            return;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

            SkillsLang.COMMAND_INFO_MESSAGE.sendMessage(player, player);
            String bonus = getBonusString(info);
            if (!bonus.isEmpty()) MessageHandler.sendMessage(sender, bonus);
        } else {
            SkillsCommandHandler.sendUsage(sender, "info <player>");
        }
    }

    public String getBonusString(SkilledPlayer info) {
        Collection<SkillsPersonalBonus> activeBonuses = info.getBonuses();
        if (activeBonuses.isEmpty()) return "";

        StringBuilder bonuses = new StringBuilder();
        boolean once = false;
        for (SkillsBonus bonus : activeBonuses) {
            SkillsLang bonusLang = bonus.getType() == SkillsEventType.SOUL ?
                    SkillsLang.COMMAND_INFO_SOUL_BONUS : SkillsLang.COMMAND_INFO_XP_BONUS;
            bonuses.append(bonusLang.parse(info.getOfflinePlayer()));
            if (activeBonuses.size() > 1 && !once) {
                bonuses.append('\n');
                once = true;
            }
        }

        return bonuses.toString();
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return args.length == 1 ? null : new String[0];
    }
}