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
import org.skills.masteries.managers.Mastery;
import org.skills.masteries.managers.MasteryManager;

public class CommandUserMastery extends SkillsCommand {
    public CommandUserMastery(SkillsCommand group) {
        super("mastery", group, SkillsLang.COMMAND_USER_MASTERY_DESCRIPTION, false, "masteries");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length >= 4) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player != null) {
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;
                Mastery mastery = MasteryManager.getMastery(args[2]);
                if (mastery == null) {
                    SkillsLang.MASTERY_NOT_FOUND.sendMessage(sender, "%mastery%", args[2]);
                    return;
                }

                try {
                    int amount = Integer.parseInt(args[3]);
                    int request = (int) HandleSimpleSetters.eval(args, info.getMasteryLevel(mastery), amount);
                    info.setMasteryLevel(mastery, request);

                    SkillsLang.Command_User_Mastery_Set_Success.sendMessage(sender,
                            "%player%", player.getName(), "%amount%", amount + "",
                            "%mastery%", mastery.getName(), "%new%", info.getMasteryLevel(mastery) + "");
                } catch (NumberFormatException ignored) {
                    SkillsCommandHandler.sendNotNumber(sender, mastery.getName() + "'s level", args[3]);
                }
            } else {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "user mastery <player> <add/decrease/set> <mastery> <amount>");
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return HandleSimpleSetters.tabComplete(args[1]);
        if (args.length == 3) return TabCompleteManager.getMasteries(args[2]);
        if (args.length == 4) return new String[]{"<amount>"};
        return new String[0];
    }
}