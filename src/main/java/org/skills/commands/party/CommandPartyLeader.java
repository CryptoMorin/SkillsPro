package org.skills.commands.party;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.party.PartyRank;
import org.skills.party.SkillsParty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandPartyLeader extends SkillsCommand {
    public CommandPartyLeader(SkillsCommand group) {
        super("leader", group, SkillsLang.COMMAND_PARTY_LEADER_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "party leader <player>");
            return;
        }

        Player leader = (Player) sender;
        SkilledPlayer leaderInfo = SkilledPlayer.getSkilledPlayer(leader);
        if (!leaderInfo.hasParty()) {
            SkillsLang.NO_PARTY.sendMessage(leader);
            return;
        }
        if (leaderInfo.getRank() != PartyRank.LEADER) {
            SkillsLang.COMMAND_PARTY_LEADER_PERMISSION.sendMessage(leader);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player == null || !player.hasPlayedBefore()) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            return;
        }

        if (player.getUniqueId().equals(leader.getUniqueId())) {
            SkillsLang.COMMAND_PARTY_LEADER_SELF.sendMessage(leader);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasParty() || !info.getPartyId().equals(leaderInfo.getPartyId())) {
            SkillsLang.COMMAND_PARTY_LEADER_NOT_IN_PARTY.sendMessage(leader, "%leader%", player.getName());
            return;
        }

        for (Player members : leaderInfo.getParty().getOnlineMembers()) {
            SkillsLang.COMMAND_PARTY_LEADER_SET.sendMessage(members, "%leader%", player.getName());
        }
        leaderInfo.setRank(PartyRank.MODERATOR);
        info.setRank(PartyRank.LEADER);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer((Player) sender);
            if (info.hasParty() && info.getRank() == PartyRank.LEADER) {
                SkillsParty party = info.getParty();
                List<String> names = new ArrayList<>();

                for (UUID members : party.getMembers()) {
                    OfflinePlayer member = Bukkit.getOfflinePlayer(members);
                    names.add(member.getName());
                }

                return names.toArray(new String[0]);
            }
        }
        return new String[0];
    }
}
