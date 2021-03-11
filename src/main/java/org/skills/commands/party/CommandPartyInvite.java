package org.skills.commands.party;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.party.PartyManager;
import org.skills.party.PartyRank;
import org.skills.party.SkillsParty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandPartyInvite extends SkillsCommand {
    public CommandPartyInvite(SkillsCommand group) {
        super("invite", group, SkillsLang.COMMAND_PARTY_INVITE_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "party invite <player>");
            return;
        }
        Player inviter = (Player) sender;
        SkilledPlayer inviterInfo = SkilledPlayer.getSkilledPlayer(inviter);
        if (!inviterInfo.hasParty()) {
            SkillsLang.NO_PARTY.sendMessage(inviter);
            return;
        }

        if (inviterInfo.getRank() == PartyRank.MEMBER) {
            SkillsLang.COMMAND_PARTY_INVITE_PERMISSION.sendMessage(inviter);
            return;
        }

        SkillsParty party = inviterInfo.getParty();
        if (party.getMembers().size() >= SkillsConfig.PARTY_MAX_MEMBERS.getInt()) {
            SkillsLang.COMMAND_PARTY_INVITE_MAX.sendMessage(inviter);
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            return;
        }

        if (player == inviter) {
            SkillsLang.COMMAND_PARTY_INVITE_SELF.sendMessage(inviter);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (info.hasParty()) {
            SkillsLang.COMMAND_PARTY_INVITE_ARLEADY_PARTYING.sendMessage(inviter, "%invited%", player.getName());
            return;
        }

        List<UUID> parties = PartyManager.INVITES.get(player.getUniqueId());
        if (parties == null) parties = new ArrayList<>();
        else if (parties.contains(inviterInfo.getPartyId())) {
            SkillsLang.COMMAND_PARTY_INVITE_ALREADY_INVITED.sendMessage(inviter);
            return;
        }

        SkillsLang.COMMAND_PARTY_INVITE_INVITED.sendMessage(inviter, "%invited%", player.getName());
        if (player.isOnline()) SkillsLang.COMMAND_PARTY_INVITE_NOTIFICATION.sendMessage(player, "%inviter%", inviter.getName(), "%party%", inviterInfo.getParty().getName());
        parties.add(inviterInfo.getPartyId());
        PartyManager.INVITES.put(player.getUniqueId(), parties);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (PartyManager.INVITES.remove(player.getUniqueId()) != null) {
                if (player.isOnline()) SkillsLang.COMMAND_PARTY_INVITE_EXPIRED.sendMessage(player, "%inviter%", inviter.getName());
            }
        }, 60 * 20L);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 0 && sender instanceof Player) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer((Player) sender);
            SkillsParty party = info.getParty();
            if (party != null) {
                if (info.getRank() != PartyRank.MEMBER) {
                    List<String> names = new ArrayList<>();

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getUniqueId().equals(((Player) sender).getUniqueId())) continue;
                        if (party.getMembers().contains(player.getUniqueId())) continue;
                        if (!args[0].isEmpty() && !player.getName().startsWith(args[0])) continue;
                        names.add(player.getName());
                    }

                    return names.toArray(new String[0]);
                }
            }
        }
        return new String[0];
    }
}
