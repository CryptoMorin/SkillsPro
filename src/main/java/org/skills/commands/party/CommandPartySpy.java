package org.skills.commands.party;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.SkillsLang;
import org.skills.party.PartyManager;

public class CommandPartySpy extends SkillsCommand {
    public CommandPartySpy(SkillsCommand group) {
        super("spy", group, SkillsLang.COMMAND_PARTY_SPY_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        if (PartyManager.SPY.remove(player.getUniqueId())) {
            SkillsLang.COMMAND_PARTY_SPY_OFF.sendMessage(player);
        } else {
            PartyManager.SPY.add(player.getUniqueId());
            SkillsLang.COMMAND_PARTY_SPY_ON.sendMessage(player);
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
