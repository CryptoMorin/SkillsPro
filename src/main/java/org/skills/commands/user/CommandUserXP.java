package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.HealthAndEnergyManager;

public class CommandUserXP extends SkillsCommand {
    public CommandUserXP(SkillsCommand group) {
        super("xp", group, SkillsLang.COMMAND_USER_XP_DESCRIPTION, false, "exp");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length >= 3) {
            if (args[0].equals("*")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                    if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;
                    int amountArg = args.length > 3 ? 3 : 2;

                    try {
                        int amount = Integer.parseInt(args[amountArg]);
                        double request;
                        double finale;
                        if (args.length > 3 && Boolean.parseBoolean(args[2])) {
                            request = HandleSimpleSetters.eval(args, info.getRawXP(), amount);
                            info.setRawXP(request, false);
                            finale = info.getRawXP();
                        } else {
                            request = HandleSimpleSetters.eval(args, info.getXP(), amount);
                            if (request < 0) info.addXP(-info.getXP() + request);
                            else info.setXP(request);
                            finale = info.getXP();
                        }

                        HandleSimpleSetters.handleSuccess(sender, SkillsLang.COMMAND_USER_XP_SUCCESS, player, amount, finale);
                        if (player.isOnline()) HealthAndEnergyManager.updateStats(player);
                    } catch (NumberFormatException e) {
                        SkillsCommandHandler.sendNotNumber(sender, "XP", args[amountArg]);
                    }
                }
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player != null) {
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;
                int amountArg = args.length > 3 ? 3 : 2;

                try {
                    int amount = Integer.parseInt(args[amountArg]);
                    double request;
                    double finale;
                    if (args.length > 3 && Boolean.parseBoolean(args[2])) {
                        request = HandleSimpleSetters.eval(args, info.getRawXP(), amount);
                        info.setRawXP(request, false);
                        finale = info.getRawXP();
                    } else {
                        request = HandleSimpleSetters.eval(args, info.getXP(), amount);
                        if (request < 0) info.addXP(-info.getXP() + request);
                        else info.setXP(request);
                        finale = info.getXP();
                    }

                    HandleSimpleSetters.handleSuccess(sender, SkillsLang.COMMAND_USER_XP_SUCCESS, player, amount, finale);
                    if (player.isOnline()) HealthAndEnergyManager.updateStats((Player) player);
                } catch (NumberFormatException e) {
                    SkillsCommandHandler.sendNotNumber(sender, "XP", args[amountArg]);
                }
            } else {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "user xp <player> <add/decrease/set> [raw] <amount>");
        }
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return HandleSimpleSetters.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<set raw?>", "true", "<amount>"};
        if (args.length == 4) return new String[]{"<amount>"};
        return new String[0];
    }
}
