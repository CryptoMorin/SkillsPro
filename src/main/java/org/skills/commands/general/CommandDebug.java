package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.DebugManager;

public class CommandDebug extends SkillsCommand {
    public CommandDebug() {
        super("debug", SkillsLang.COMMAND_DEBUG_DESCRIPTION);
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        if (DebugManager.ACTIVE.put(player.getEntityId(), player) == null) {
            SkillsLang.COMMAND_DEBUG_ON.sendMessage(player);
        } else {
            SkillsLang.COMMAND_DEBUG_OFF.sendMessage(player);
            DebugManager.ACTIVE.remove(player.getEntityId());
        }
    }

    @Override
    public @Nullable String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[0];
    }
}
