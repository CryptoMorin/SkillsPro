package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

public class CommandUpdates extends SkillsCommand {
    public CommandUpdates() {
        super("updates", SkillsLang.COMMAND_UPDATES_DESCRIPTION, false, "update");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) MessageHandler.sendPluginMessage(sender, "&2Checking for updates...");
        plugin.getUpdater().checkForUpdates().thenRunAsync(() -> {
            if (plugin.getUpdater().canUpdate()) {
                MessageHandler.sendPluginMessage(sender, plugin.getUpdater().updateText());
            } else
                MessageHandler.sendPluginMessage(sender, "&2No updates found &8- &8(&6v" + plugin.getUpdater().currentVersion + "&8)");
        });
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
