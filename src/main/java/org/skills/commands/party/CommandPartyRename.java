package org.skills.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;

public class CommandPartyRename extends SkillsCommand {
    public CommandPartyRename(SkillsCommand group) {
        super("rename", group, SkillsLang.COMMAND_PARTY_RENAME_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            SkillsLang.COMMAND_PARTY_RENAME_NAME.sendMessage(player);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasParty()) {
            SkillsLang.NO_PARTY.sendMessage(player);
            return;
        }

        info.getParty().setName(args[0]);
        for (Player members : info.getParty().getOnlineMembers()) {
            SkillsLang.COMMAND_PARTY_RENAME_RENAMED.sendMessage(members, "%name%", args[0]);
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
