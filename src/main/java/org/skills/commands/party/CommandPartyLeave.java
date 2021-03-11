package org.skills.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.party.PartyRank;
import org.skills.party.SkillsParty;
import org.skills.utils.Cooldown;

import java.util.concurrent.TimeUnit;

public class CommandPartyLeave extends SkillsCommand {
    public CommandPartyLeave(SkillsCommand group) {
        super("leave", group, SkillsLang.COMMAND_PARTY_LEAVE_DESCRIPTION);
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
        SkillsParty party = info.getParty();

        if (info.getRank() == PartyRank.LEADER) {
            if (Cooldown.isInCooldown(player.getUniqueId(), "PARTY_DISBAND")) {
                for (Player member : party.getOnlineMembers()) {
                    SkillsLang.COMMAND_PARTY_LEAVE_DISBANDED.sendMessage(member, "%party%", party.getName());
                }
                party.disband();
            } else {
                new Cooldown(player.getUniqueId(), "PARTY_DISBAND", 30, TimeUnit.SECONDS);
                SkillsLang.COMMAND_PARTY_LEAVE_DISBAND_CONFIRM.sendMessage(player);
            }

            return;
        }

        for (Player member : party.getOnlineMembers()) {
            SkillsLang.COMMAND_PARTY_LEAVE_LEFT.sendMessage(member, player);
        }
        info.leaveParty();
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
