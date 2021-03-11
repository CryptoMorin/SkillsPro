package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;

public class CommandShowActiveMessages extends SkillsCommand {
    public CommandShowActiveMessages() {
        super("showactivemessage", SkillsLang.COMMAND_SHOWACTIVEMESSAGES_DESCRIPTION, false, "sam");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer((Player) sender);
            info.setShowReadyMessage(!info.showReadyMessage());

            if (info.showReadyMessage()) {
                SkillsLang.COMMAND_SHOWACTIVEMESSAGES_ON.sendMessage(sender);
            } else {
                SkillsLang.COMMAND_SHOWACTIVEMESSAGES_OFF.sendMessage(sender);
            }

        } else {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
        }
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
