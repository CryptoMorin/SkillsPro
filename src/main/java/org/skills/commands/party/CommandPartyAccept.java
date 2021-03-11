package org.skills.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.party.PartyManager;
import org.skills.party.SkillsParty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandPartyAccept extends SkillsCommand {
    public CommandPartyAccept(SkillsCommand group) {
        super("accept", group, SkillsLang.COMMAND_PARTY_ACCEPT_DESCRIPTION, "join");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (info.hasParty()) {
            SkillsLang.COMMAND_PARTY_ACCEPT_IN_PARTY.sendMessage(player);
            return;
        }

        List<UUID> parties = PartyManager.INVITES.get(player.getUniqueId());
        if (parties == null) {
            SkillsLang.COMMAND_PARTY_ACCEPT_NOT_INVITED.sendMessage(player);
            return;
        }

        SkillsParty party = null;
        if (args.length == 0) {
            if (parties.size() == 1) {
                party = SkillsParty.getParty(parties.get(0));
            } else {
                SkillsLang.COMMAND_PARTY_ACCEPT_NAME.sendMessage(player);
                return;
            }
        } else {
            for (UUID inviters : parties) {
                SkillsParty inviter = SkillsParty.getParty(inviters);
                if (inviter.getName().equalsIgnoreCase(args[0])) party = inviter;
            }
        }

        if (party != null) {
            PartyManager.INVITES.remove(player.getUniqueId());
            info.joinParty(party);

            for (Player member : party.getOnlineMembers()) {
                SkillsLang.COMMAND_PARTY_ACCEPT_JOINED.sendMessage(member, player);
            }
        } else {
            if (args.length == 0) SkillsLang.COMMAND_PARTY_ACCEPT_NOT_INVITED.sendMessage(player);
            else SkillsLang.COMMAND_PARTY_ACCEPT_NOT_INVITED_SPECIFIC.sendMessage(player, "%party%", args[0]);
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        List<String> names = new ArrayList<>();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<UUID> invites = PartyManager.INVITES.get(player.getUniqueId());
            if (invites == null || invites.isEmpty()) return new String[0];

            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
            if (!info.hasParty()) {
                for (UUID partyId : invites) {
                    SkillsParty party = SkillsParty.getParty(partyId);
                    if (party != null) names.add(party.getName());
                }
            }

            return names.toArray(new String[0]);
        }
        return new String[0];
    }
}
