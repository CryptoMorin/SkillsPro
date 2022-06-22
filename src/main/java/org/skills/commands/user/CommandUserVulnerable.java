package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.main.locale.SkillsLang;

public class CommandUserVulnerable extends SkillsCommand {
    public CommandUserVulnerable(SkillsCommand group) {
        super("vulnerable", group, SkillsLang.COMMAND_USER_VULNERABLE_DESCRIPTION, false);
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "user vulnerable <player>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            return;
        }
        if (!player.isInvulnerable()) {
            SkillsLang.COMMAND_USER_VULNERABLE_ALREADY_VULNERABLE.sendMessage(sender, "%target%", player.getName());
            return;
        }

        player.setInvulnerable(false);
        SkillsLang.COMMAND_USER_VULNERABLE_SET.sendMessage(sender, "%target%", player.getName());
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        return new String[0];
    }
}
