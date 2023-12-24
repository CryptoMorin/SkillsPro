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

public class CommandPartyKick extends SkillsCommand {
    public CommandPartyKick(SkillsCommand group) {
        super("kick", group, SkillsLang.COMMAND_PARTY_KICK_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "party kick <player>");
            return;
        }
        Player kicker = (Player) sender;
        SkilledPlayer kickerInfo = SkilledPlayer.getSkilledPlayer(kicker);
        if (!kickerInfo.hasParty()) {
            SkillsLang.NO_PARTY.sendMessage(kicker);
            return;
        }
        if (kickerInfo.getRank() == PartyRank.MEMBER) {
            SkillsLang.COMMAND_PARTY_KICK_PERMISSION.sendMessage(kicker);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player == null || !player.hasPlayedBefore()) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            return;
        }

        if (player.getUniqueId().equals(kicker.getUniqueId())) {
            SkillsLang.COMMAND_PARTY_KICK_SELF.sendMessage(kicker);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasParty()) {
            SkillsLang.COMMAND_PARTY_KICK_NOT_IN_PARTY.sendMessage(kicker, "%kicked%", player.getName());
            return;
        }

        for (UUID members : info.getParty().getMembers()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(members);
            if (member.isOnline())
                SkillsLang.COMMAND_PARTY_KICK_KICKED.sendMessage((Player) member, player, "%kicked%", player.getName(), "%kicker%", kicker.getName());
        }
        info.leaveParty();
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer((Player) sender);
            if (info.hasParty() && info.getRank() != PartyRank.MEMBER) {
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
