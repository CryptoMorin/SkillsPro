package org.skills.commands.general;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.PlayerDataManager;
import org.skills.main.locale.SkillsLang;

public class CommandTop extends SkillsCommand {
    public CommandTop() {
        super("top", SkillsLang.COMMAND_TOP_DESCRIPTION, false);
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        PlayerDataManager.getTopLevels(10).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        }).thenAccept((result) -> {
            SkillsLang.COMMAND_TOP_HEADER.sendMessage(sender);
            int size = result.size();
            for (int i = size - 1; i >= 0; i--) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(result.get(i));
                if (new PermissibleBase(player).hasPermission("skills.command.top.exclude")) continue;

                SkillsLang.COMMAND_TOP_ENTRY.sendMessage(sender, player,
                        "%number%", (size - i),
                        "%name%", (player.getName() == null ? "~Unknown" : player.getName()));
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
