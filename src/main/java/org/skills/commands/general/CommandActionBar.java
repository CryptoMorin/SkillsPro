package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;

public class CommandActionBar extends SkillsCommand {
    public CommandActionBar() {
        super("actionbar", SkillsLang.COMMAND_SHOW_ACTION_BAR_DESCRIPTION, false, "ab");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer((Player) sender);
            info.setShowActionBar(!info.showActionBar());

            if (info.showActionBar()) {
                SkillsLang.COMMAND_SHOW_ACTION_BAR_ON.sendMessage(sender);
            } else {
                SkillsLang.COMMAND_SHOW_ACTION_BAR_OFF.sendMessage(sender);
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
