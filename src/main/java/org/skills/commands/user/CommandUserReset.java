package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.party.PartyRank;

public class CommandUserReset extends SkillsCommand {
    public CommandUserReset(SkillsCommand group) {
        super("reset", group, SkillsLang.COMMAND_USER_RESET_DESCRIPTION, false, "resetplayer", "playerreset", "resetinfo", "inforeset");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player != null) {
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player.getUniqueId());
                if (info.getRank() == PartyRank.LEADER) info.getParty().disband();
                plugin.getPlayerDataManager().delete(player.getUniqueId());
                SkillsLang.COMMAND_USER_RESET_SUCCESS.sendMessage(sender, "%player%", player.getName());
            } else {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "user reset <player>");
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return args.length > 1 ? new String[0] : null;
    }
}