package org.skills.commands.general;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

public class CommandAbout extends SkillsCommand {
    public static final String USER = "%%__USER__%%";
    public static final String NONCE = "%%__NONCE__%%";

    public CommandAbout() {
        super("about", SkillsLang.COMMAND_ABOUT_DESCRIPTION, false, "version");
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        String register = USER.startsWith("%%__USER__%") && USER.endsWith("%%") ? "&4Unrecognized" :
                "&9" + USER + " &8(&2" + NONCE + "&8)";
        MessageHandler.sendMessage(sender, "\n&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n\n" +
                "             &7-=( &3SkillsPro &7)=-\n" +
                "&7| &2" + plugin.getDescription().getDescription() + '\n' +
                "&7| &2Developers&8: &9Crypto Morin & Hex_26\n" +
                "&7| &2Version&8: &9" + plugin.getDescription().getVersion() + '\n' +
                "&7| &2Register&8: " + register + "\n\n" +
                "&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n ");
    }

    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[0];
    }
}
