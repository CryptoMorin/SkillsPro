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
import org.skills.main.locale.SkillsLang;

import java.util.ArrayList;
import java.util.List;

public class CommandUnfriend extends SkillsCommand {
    public CommandUnfriend() {
        super("unfriend", SkillsLang.COMMAND_UNFRIEND_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        if (args.length < 1) {
            SkillsCommandHandler.sendUsage(sender, "unfriend <player>");
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player == null || !player.hasPlayedBefore()) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            return;
        }

        Player inviter = (Player) sender;
        if (player.getUniqueId().equals(inviter.getUniqueId())) {
            SkillsLang.COMMAND_UNFRIEND_SELF.sendMessage(inviter);
            return;
        }
        SkilledPlayer inviterInfo = SkilledPlayer.getSkilledPlayer(inviter);
        if (!inviterInfo.getFriends().contains(player.getUniqueId())) {
            SkillsLang.COMMAND_UNFRIEND_INVALID.sendMessage(inviter, "%removed%", player.getName());
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        info.getFriends().remove(inviter.getUniqueId());
        inviterInfo.getFriends().remove(player.getUniqueId());

        SkillsLang.COMMAND_UNFRIEND_REMOVED.sendMessage(inviter, "%removed%", player.getName());
        if (player.isOnline())
            SkillsLang.COMMAND_UNFRIEND_NOTIFICATION.sendMessage((Player) player, "%remover%", inviter.getName());
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        List<String> players = new ArrayList<>();
        if (sender instanceof Player) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(((Player) sender).getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getUniqueId().equals(((Player) sender).getUniqueId())) continue;
                if (!info.getFriends().contains(online.getUniqueId())) continue;
                if (args[0].isEmpty() || !online.getName().startsWith(args[0])) continue;
                players.add(online.getName());
            }

            return players.toArray(new String[0]);
        }

        return new String[0];
    }
}
