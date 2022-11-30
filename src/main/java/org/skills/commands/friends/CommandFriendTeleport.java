package org.skills.commands.friends;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandFriendTeleport extends SkillsCommand {
    public CommandFriendTeleport() {
        super("friendtp", SkillsLang.COMMAND_FRIENDTP_DESCRIPTION, "tp", "tpfriend", "teleport");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "friendtp <player>");
            return;
        }

        Player player = (Player) sender;
        if (!SkillsConfig.FRIENDS_TELEPORT_ENABLED.getBoolean()) {
            SkillsLang.COMMAND_FRIENDTP_DISABLED.sendMessage(player);
            return;
        }

        Player tpTo = Bukkit.getPlayer(args[0]);
        if (tpTo == null) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(player, "%name%", args[0]);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.getFriends().contains(tpTo.getUniqueId())) {
            SkillsLang.COMMAND_FRIENDTP_NOT_FRIENDS.sendMessage(player, "%name%", tpTo.getName());
            return;
        }

        if (player.hasPermission("skills.command.friendtp.instant")) {
            player.teleport(tpTo);
            SkillsLang.COMMAND_FRIENDTP_TELEPORTED.sendMessage(player, "%friend%", tpTo.getName());
            SkillsLang.COMMAND_FRIENDTP_NOTIFY.sendMessage(tpTo, "%friend%", player.getName());
            return;
        }

        Set<CommandFriendAccept.Teleportation> sentReqs = CommandFriendAccept.requests.getOrDefault(tpTo.getUniqueId(), new HashSet<>());
        if (sentReqs.stream().anyMatch(x -> x.from.equals(player.getUniqueId()))) {
            SkillsLang.COMMAND_FRIENDTP_ALREADY_REQUESTED.sendMessage(player, "%friend%", tpTo.getName());
            return;
        }

        SkillsLang.COMMAND_FRIENDTP_REQUESTED.sendMessage(player, "%friend%", tpTo.getName());
        SkillsLang.COMMAND_FRIENDTP_NOTIFY_REQUEST.sendMessage(tpTo, "%friend%", player.getName());

        int time = SkillsConfig.FRIENDS_TELEPORT_REQUEST_TIME.getInt();
        int id = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Set<CommandFriendAccept.Teleportation> sentReqsNew = CommandFriendAccept.requests.getOrDefault(tpTo.getUniqueId(), new HashSet<>());
            CommandFriendAccept.Teleportation request = sentReqsNew.stream().filter(x -> x.from.equals(player.getUniqueId())).findFirst().orElse(null);
            if (request != null) {
                if (player.isOnline()) SkillsLang.COMMAND_FRIENDTP_EXPIRED.sendMessage(player, "%friend%", tpTo.getName());
                if (tpTo.isOnline()) SkillsLang.COMMAND_FRIENDTP_EXPIRED_NOTIFY.sendMessage(tpTo, "%friend%", player.getName());
                sentReqsNew.remove(request);
                if (sentReqsNew.isEmpty()) CommandFriendAccept.requests.remove(tpTo.getUniqueId());
                else CommandFriendAccept.requests.put(tpTo.getUniqueId(), sentReqsNew);
            }
        }, time * 20L).getTaskId();

        sentReqs.add(new CommandFriendAccept.Teleportation(player.getUniqueId(), id));
        CommandFriendAccept.requests.put(tpTo.getUniqueId(), sentReqs);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        List<String> players = new ArrayList<>();
        if (sender instanceof Player) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(((Player) sender).getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getUniqueId().equals(((Player) sender).getUniqueId())) continue;
                if (info.getFriends().contains(online.getUniqueId())) players.add(online.getName());
            }

            return players.toArray(new String[0]);
        }

        return new String[0];
    }
}