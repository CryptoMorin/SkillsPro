package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.types.SkillScaling;

public class CommandRefresh extends SkillsCommand {
    public CommandRefresh() {
        super("refresh", SkillsLang.COMMAND_REFRESH_DESCRIPTION, false);
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(p);
            info.setEnergy(info.getScaling(SkillScaling.MAX_ENERGY));
            info.setCooldown(0);
            SkillsLang.Command_Refresh_Success.sendMessage(p);
        } else {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
