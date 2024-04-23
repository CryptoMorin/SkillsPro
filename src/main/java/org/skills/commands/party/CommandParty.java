package org.skills.commands.party;

import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;
import org.skills.main.locale.SkillsLang;

public class CommandParty extends SkillsCommand {
    public CommandParty() {
        super("party", SkillsLang.COMMAND_PARTY_DESCRIPTION, true, "parties");

        new CommandPartyCreate(this);
        new CommandPartyRename(this);
        new CommandPartyInvite(this);
        new CommandPartyKick(this);
        new CommandPartyList(this);
        new CommandPartyLeave(this);
        new CommandPartyChat(this);
        new CommandPartyAccept(this);
        new CommandPartyMod(this);
        new CommandPartyLeader(this);
        new CommandPartySpy(this);
        new CommandPartyParties(this);
        // new CommandPartyDeny(this);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        SkillsCommandHandler.executeHelperForGroup(this, sender);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return args.length < 2 ? TabCompleteManager.getSubCommand(sender, this, args).toArray(new String[0]) : new String[0];
    }
}
