package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.Cosmetic;
import org.skills.data.managers.CosmeticCategory;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

public class CommandCosmetic extends SkillsCommand {
    private final CosmeticCategory category;

    public CommandCosmetic(CosmeticCategory category) {
        super(category.getCommand(), category.getDescription());
        this.category = category;
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        InteractiveGUI gui = GUIParser.parseOption(player, category.getName());
        if (gui == null) {
            MessageHandler.sendPlayerMessage(player, "&4Could not find/parse GUI for &e" + category.getName() + " &4cosmetic category. " +
                    "Please contact the server's administrators. If you're an admin, please also check your console.");
            return;
        }

        for (Cosmetic cosmetic : category.getCosmetics().values()) {
            gui.push(cosmetic.getName(), () -> {
                info.getCosmetics().put(category.getName(), cosmetic);
                player.closeInventory();
            });
        }

        gui.setRest();
        gui.openInventory(player);
    }

    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[0];
    }
}
