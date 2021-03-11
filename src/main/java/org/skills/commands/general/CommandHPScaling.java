package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;

public class CommandHPScaling extends SkillsCommand {
    public CommandHPScaling() {
        super("hpscaling", null, SkillsLang.COMMAND_SCALING_DESCRIPTION, "scaling", "healthscaling", "hp", "health");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendMessage(sender);
            return;
        }
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "<scale>");
            return;
        }
        String num = args[0];
        double scaling;
        try {
            scaling = Integer.parseInt(num);
        } catch (NumberFormatException ex) {
            SkillsLang.Command_Not_Number.sendMessage(sender, "%args%", num, "%needed%", "Health Scaling");
            return;
        }
        if (scaling < 0 || scaling > 80) {
            SkillsLang.COMMAND_SCALING_NOT_IN_RANGE.sendMessage(sender);
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        info.setHealthScaling(scaling);
        info.setScaledHealth();
        SkillsLang.COMMAND_SCALING_SET.sendMessage(player, "%scale%", scaling);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[]{"0-80"};
    }
}
