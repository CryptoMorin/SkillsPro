package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;

public class CommandUserSouls extends SkillsCommand {
    public CommandUserSouls(SkillsCommand group) {
        super("souls", group, SkillsLang.COMMAND_USER_SOULS_DESCRIPTION, false, "soul");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length >= 3) {
            if (args[0].equals("*")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                    if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;

                    try {
                        int amount = Integer.parseInt(args[2]);
                        int request = (int) HandleSimpleSetters.eval(args, info.getSouls(), amount);
                        info.setSouls(request);

                        HandleSimpleSetters.handleSuccess(sender, SkillsLang.COMMAND_USER_SOULS_SUCCESS, player, amount, info.getSouls());
                    } catch (NumberFormatException e) {
                        SkillsCommandHandler.sendNotNumber(sender, "Soul", args[2]);
                    }
                }
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player != null) {
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;

                try {
                    int amount = Integer.parseInt(args[2]);
                    int request = (int) HandleSimpleSetters.eval(args, info.getSouls(), amount);
                    info.setSouls(request);

                    HandleSimpleSetters.handleSuccess(sender, SkillsLang.COMMAND_USER_SOULS_SUCCESS, player, amount, info.getSouls());
                } catch (NumberFormatException e) {
                    SkillsCommandHandler.sendNotNumber(sender, "Soul", args[2]);
                }
            } else {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "user soul <player> <add/decrease/set> <amount>");
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return HandleSimpleSetters.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<amount>"};
        return new String[0];
    }
}
