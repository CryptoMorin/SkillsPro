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

public class CommandPartyMod extends SkillsCommand {
    public CommandPartyMod(SkillsCommand group) {
        super("mod", group, SkillsLang.COMMAND_PARTY_MOD_DESCRIPTION, "moderator");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "party mod <player>");
            return;
        }

        Player leader = (Player) sender;
        SkilledPlayer leaderInfo = SkilledPlayer.getSkilledPlayer(leader);
        if (!leaderInfo.hasParty()) {
            SkillsLang.NO_PARTY.sendMessage(leader);
            return;
        }
        if (leaderInfo.getRank() != PartyRank.LEADER) {
            SkillsLang.COMMAND_PARTY_MOD_PERMISSION.sendMessage(leader);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player == null || !player.hasPlayedBefore()) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            return;
        }

        if (player.getUniqueId().equals(leader.getUniqueId())) {
            SkillsLang.COMMAND_PARTY_MOD_SELF.sendMessage(leader);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasParty() || !info.getPartyId().equals(leaderInfo.getPartyId())) {
            SkillsLang.COMMAND_PARTY_MOD_NOT_IN_PARTY.sendMessage(leader, "%mod%", player.getName());
            return;
        }

        if (info.getRank() == PartyRank.MEMBER) {
            info.setRank(PartyRank.MODERATOR);
            for (Player members : leaderInfo.getParty().getOnlineMembers()) {
                SkillsLang.COMMAND_PARTY_MOD_PROMOTED.sendMessage(members, "%mod%", player.getName());
            }
        } else {
            info.setRank(PartyRank.MEMBER);
            for (Player members : leaderInfo.getParty().getOnlineMembers()) {
                SkillsLang.COMMAND_PARTY_MOD_DEMOTED.sendMessage(members, "%mod%", player.getName());
            }
        }
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
                    SkilledPlayer memberInfo = SkilledPlayer.getSkilledPlayer(member);
                    if (memberInfo.getRank() == PartyRank.MEMBER) names.add(member.getName());
                }

                return names.toArray(new String[0]);
            }
        }
        return new String[0];
    }
}
