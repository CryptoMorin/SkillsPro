package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.types.SkillManager;

import java.util.Arrays;

public class CommandUserSkill extends SkillsCommand {
    public CommandUserSkill(SkillsCommand group) {
        super("skill", group, SkillsLang.COMMAND_USER_SKILL_DESCRIPTION, false, "setskill");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            SkillsCommandHandler.sendUsage(sender, "user skill <player> <skill>");
            return;
        }
        String name = args[0];
        String skill = args[1].toLowerCase();
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        if (!player.hasPlayedBefore()) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!SkillManager.isSkillRegistered(skill)) {
            SkillsLang.COMMAND_USER_SKILL_NOT_SKILL.sendMessage(sender, "%skill%", skill);
            return;
        }

        if (info.setActiveSkill(SkillManager.getSkill(skill)).isCancelled()) return;
        SkillsLang.COMMAND_USER_SKILL_SUCCESS.sendMessage(sender, "%player%", name, "%skill%", skill);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) {
            String[] names = TabCompleteManager.getSkillTypes(args[1]).toArray(new String[0]);
            if (args[1].isEmpty()) return names;
            else return Arrays.stream(names).filter(x -> x.toLowerCase().startsWith(args[1].toLowerCase())).toArray(String[]::new);
        }
        return new String[0];
    }
}
