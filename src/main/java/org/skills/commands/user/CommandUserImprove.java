package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;

public class CommandUserImprove extends SkillsCommand {
    public CommandUserImprove(SkillsCommand group) {
        super("improve", group, SkillsLang.COMMAND_USER_IMPROVE_DESCRIPTION, false, "improvement", "improvements", "upgrade", "upgrades", "ability", "abilities");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length >= 4) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player != null) {
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;
                Ability ability = info.getSkill().getAbility(args[2]);
                if (ability == null) {
                    SkillsLang.ABILITY_NOT_FOUND.sendMessage(sender, "%ability%", args[2]);
                    return;
                }

                try {
                    int amount = Integer.parseInt(args[3]);
                    int request = (int) HandleSimpleSetters.eval(args, info.getImprovementLevel(ability), amount);
                    if (request < 0 || request > 3) {
                        SkillsLang.ABILITY_INVALID_LEVEL.sendMessage(sender);
                        return;
                    }
                    info.setImprovement(ability, request);

                    SkillsLang.COMMAND_USER_IMPROVEMENT_SUCCESS.sendMessage(sender,
                            "%player%", player.getName(), "%amount%", amount,
                            "%ability%", ability.getName(), "%new%", info.getImprovementLevel(ability));
                } catch (NumberFormatException ignored) {
                    SkillsCommandHandler.sendNotNumber(sender, ability.getName() + "'s level", args[3]);
                }
            } else {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "user improvement <player> <add/decrease/set> <improvement> <amount>");
        }
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return HandleSimpleSetters.tabComplete(args[1]);
        if (args.length == 3) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player == null || !player.hasPlayedBefore()) return new String[0];
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
            if (info == null) return new String[0];
            return info.getSkill().getAbilities().stream().map(Ability::getName).filter(x -> x.startsWith(args[2])).toArray(String[]::new);
        }
        if (args.length == 4) return new String[]{"<amount>"};
        return new String[0];
    }
}
