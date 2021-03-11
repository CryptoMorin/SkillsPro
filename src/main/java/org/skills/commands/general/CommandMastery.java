package org.skills.commands.general;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.masteries.managers.MasteryGUIManager;

public class CommandMastery extends SkillsCommand {
    public CommandMastery() {
        super("mastery", SkillsLang.COMMAND_MASTERY_DESCRIPTION, false, "masteries");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        OfflinePlayer player;
        if (args.length == 0) player = (Player) sender;
        else {
            if (!sender.hasPermission("skills.command.mastery.others")) {
                SkillsLang.COMMAND_MASTERY_OTHERS_PERMISSION.sendMessage(sender);
                return;
            }

            player = getPlayer(sender, args[0]);
            if (player == null) return;
        }

        MasteryGUIManager.openMenu(SkilledPlayer.getSkilledPlayer(player), (Player) sender, false);
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
