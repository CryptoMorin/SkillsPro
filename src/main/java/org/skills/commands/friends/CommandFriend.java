package org.skills.commands.friends;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import java.util.List;

public class CommandFriend extends SkillsCommand {
    public CommandFriend() {
        super("friend", SkillsLang.COMMAND_FRIEND_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "friend <player>");
            return;
        }

        Player inviter = (Player) sender;
        SkilledPlayer inviterInfo = SkilledPlayer.getSkilledPlayer(inviter);
        if (inviterInfo.getFriends().size() >= SkillsConfig.FRIENDS_MAX_FRIENDS.getInt()) {
            SkillsLang.COMMAND_FRIEND_MAX.sendMessage(inviter);
            return;
        }

        OfflinePlayer player = getPlayer(sender, args[0]);
        if (player == null) return;

        if (player.getUniqueId().equals(inviter.getUniqueId())) {
            SkillsLang.COMMAND_FRIEND_SELF.sendMessage(inviter);
            return;
        }

        if (inviterInfo.getFriends().contains(player.getUniqueId())) {
            SkillsLang.COMMAND_FRIEND_ALREADY_FRIENDS.sendMessage(inviter, "%invited%", player.getName());
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (info.getFriendRequests().contains(inviter.getUniqueId())) {
            SkillsLang.COMMAND_FRIEND_ALREADY_SENT.sendMessage(inviter, "%invited%", player.getName());
            return;
        }
        if (inviterInfo.getFriendRequests().contains(player.getUniqueId())) {
            if (player.isOnline())
                SkillsLang.COMMAND_FRIEND_FRIENDED.sendMessage((Player) player, "%friend%", inviter.getName());
            SkillsLang.COMMAND_FRIEND_FRIENDED.sendMessage(inviter, "%friend%", player.getName());

            info.getFriendRequests().remove(inviter.getUniqueId());
            inviterInfo.getFriendRequests().remove(player.getUniqueId());
            info.getFriends().add(inviter.getUniqueId());
            inviterInfo.getFriends().add(player.getUniqueId());
            return;
        }

        info.friendRequest(inviter);
        SkillsLang.COMMAND_FRIEND_REQUEST.sendMessage(inviter, "%invited%", player.getName());
        if (player.isOnline())
            SkillsLang.COMMAND_FRIEND_NOTIFICATION.sendMessage((Player) player, "%inviter%", inviter.getName());
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        List<String> players = new ArrayList<>();
        if (sender instanceof Player) {
            if (args.length == 1) {
                Player player = (Player) sender;
                SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.getUniqueId().equals((player.getUniqueId()))) continue;
                    if (info.getFriends().contains(online.getUniqueId())) continue;
                    if (!args[0].isEmpty() && !online.getName().startsWith(args[0])) continue;
                    players.add(online.getName());
                }

                return players.toArray(new String[0]);
            }
        }

        return new String[0];
    }
}
