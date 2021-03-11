package org.skills.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.party.PartyManager;

public class CommandPartyChat extends SkillsCommand {
    public CommandPartyChat(SkillsCommand group) {
        super("chat", group, SkillsLang.COMMAND_PARTY_CHAT_DESCRIPTION, "toggle");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasParty()) {
            SkillsLang.NO_PARTY.sendMessage(player);
            return;
        }

        if (PartyManager.CHAT.remove(player.getUniqueId())) {
            SkillsLang.COMMAND_PARTY_CHAT_DISABLED.sendMessage(player);
        } else {
            PartyManager.CHAT.add(player.getUniqueId());
            SkillsLang.COMMAND_PARTY_CHAT_ENABLED.sendMessage(player);
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
