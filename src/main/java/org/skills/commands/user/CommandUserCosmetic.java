package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.Cosmetic;
import org.skills.data.managers.CosmeticCategory;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;

import java.util.ArrayList;
import java.util.List;

public class CommandUserCosmetic extends SkillsCommand {
    public CommandUserCosmetic(SkillsCommand group) {
        super("cosmetic", group, SkillsLang.COMMAND_USER_COSMETIC_DESCRIPTION, false, "cosmetics", "cos");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            SkillsCommandHandler.sendUsage(sender, "user cosmetic <player> <category> <cosmetic>");
            return;
        }
        String name = args[0];
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        if (!player.hasPlayedBefore()) {
            SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        CosmeticCategory cat = CosmeticCategory.get(args[1]);
        if (cat == null) {
            SkillsLang.COMMAND_USER_COSMETIC_INVALID_CATEGORY.sendMessage(sender, "%category%", args[1], "%cosmetic%", args[2]);
            return;
        }

        Cosmetic cosmetic = cat.getCosmetic(args[2]);
        if (cosmetic == null) {
            SkillsLang.COMMAND_USER_COSMETIC_INVALID_COSMETIC.sendMessage(sender, "%category%", args[1], "%cosmetic%", args[2]);
            return;
        }

        if (info.getCosmetic(cat.getName()) == cosmetic) {
            SkillsLang.COMMAND_USER_COSMETIC_ALREADY_SET.sendMessage(sender, "%category%", args[1], "%cosmetic%", args[2]);
            return;
        }

        info.setCosmetic(cosmetic);
        SkillsLang.COMMAND_USER_COSMETIC_SET.sendMessage(sender, "%category%", args[1], "%cosmetic%", args[2]);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) {
            List<String> categories = new ArrayList<>(CosmeticCategory.getCategories().keySet());
            return categories.stream().filter(x -> x.toLowerCase().startsWith(args[1].toLowerCase())).toArray(String[]::new);
        }
        if (args.length == 3) {
            CosmeticCategory cat = CosmeticCategory.get(args[1]);
            if (cat == null) return new String[0];
            return cat.getCosmetics().keySet().toArray(new String[0]);
        }
        return new String[0];
    }
}
