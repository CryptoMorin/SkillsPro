package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.types.Stat;

public class CommandUserStats extends SkillsCommand {
    public CommandUserStats(SkillsCommand group) {
        super("stats", group, SkillsLang.COMMAND_USER_STATS_DESCRIPTION, false, "stat", "statpoints", "statspoint");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length >= 3) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player != null && player.hasPlayedBefore()) {
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;

                try {
                    int amount = Integer.parseInt(args[2]);
                    int request = (int) HandleSimpleSetters.eval(args, info.getPoints(), amount);
                    info.setStat(Stat.POINTS, request);

                    HandleSimpleSetters.handleSuccess(sender, SkillsLang.COMMAND_USER_STATPOINTS_SUCCESS, player, amount, info.getPoints());
                } catch (NumberFormatException e) {
                    SkillsCommandHandler.sendNotNumber(sender, "Stats", args[2]);
                }
            } else {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "user stats <player> <add/decrease/set> <amount>");
        }
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return HandleSimpleSetters.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<amount>"};
        return new String[0];
    }
}