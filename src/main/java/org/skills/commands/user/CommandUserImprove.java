package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;

public class CommandUserImprove extends SkillsCommand {
    public CommandUserImprove(SkillsCommand group) {
        super("improve", group, SkillsLang.COMMAND_USER_IMPROVE_DESCRIPTION, false, "improvement", "improvements", "upgrade", "upgrades", "ability", "abilities");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        CommandUser.handle(this, sender, args, "ability", (changeFactory, player, info, type, silent) -> {
            Ability ability = info.getSkill().getAbility(type);
            if (ability == null) {
                SkillsLang.ABILITY_NOT_FOUND.sendMessage(sender, "%ability%", type, "%player%", player.getName());
                return false;
            }

            int request = (int) changeFactory.withInitialAmount(info.getAbilityLevel(ability)).getFinalAmount();
            if (changeFactory.getType() == AmountChangeFactory.Type.SET && (request < 0 || request > 3)) {
                SkillsLang.ABILITY_INVALID_LEVEL.sendMessage(sender);
                return false;
            }

            info.setAbilityLevel(ability, Math.max(0, Math.min(3, request)));
            return true;
        });
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return AmountChangeFactory.tabComplete(args[1]);
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
