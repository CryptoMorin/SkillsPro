package org.skills.commands.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.PlayerDataManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.HealthAndEnergyManager;
import org.skills.managers.LevelUp;
import org.skills.types.SkillScaling;

public class CommandUserLevel extends SkillsCommand {
    public CommandUserLevel(SkillsCommand group) {
        super("level", group, SkillsLang.COMMAND_USER_LEVEL_DESCRIPTION, false, "lvl", "lvls", "levels");
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length >= 3) {
            if (args[0].equals("*")) {
                if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                        int pre = info.getLevel();
                        int amount = Integer.parseInt(args[2]);
                        int request = (int) HandleSimpleSetters.eval(args, info.getLevel(), amount);
                        info.setLevel(request);
                        if (info.getXP() > info.getLevelXP()) info.setAbsoluteXP(0);
                        if (request > pre) {
                            boolean silent = false;
                            if (args.length > 3 && args[3].equalsIgnoreCase("silent")) silent = true;

                            if (!silent) {
                                int currentLvl = info.getLevel();
                                LevelUp level = null;
                                for (int lvl = 0; lvl < currentLvl; lvl++) {
                                    level = LevelUp.getProperties(request).evaluate(info, lvl);
                                    level.perform(info, "%next_maxxp%", info.getLevelXP(request));
                                }
                                if (level != null && player.isOnline()) level.celebrate(player, plugin, request);
                            }
                        }

                        PlayerDataManager.addLevel(player, request);
                        HandleSimpleSetters.handleSuccess(sender, SkillsLang.COMMAND_USER_LEVEL_SUCCESS, player, amount, info.getLevel());
                        if (player.isOnline()) {
                            double maxEnergy = info.getScaling(SkillScaling.MAX_ENERGY);
                            if (info.getEnergy() > maxEnergy) info.setEnergy(maxEnergy);
                            HealthAndEnergyManager.updateStats(player);
                        }
                    } catch (NumberFormatException e) {
                        SkillsCommandHandler.sendNotNumber(sender, "Level", args[2]);
                    }
                }
                return;
            }


            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player.hasPlayedBefore()) {
                if (HandleSimpleSetters.handleInvalidSetter(sender, args)) return;

                try {
                    SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
                    int pre = info.getLevel();
                    int amount = Integer.parseInt(args[2]);
                    int request = (int) HandleSimpleSetters.eval(args, info.getLevel(), amount);
                    int maxLvl = (int) info.getScaling(SkillScaling.MAX_LEVEL);

                    if (pre == maxLvl) {
                        if (player.isOnline()) SkillsLang.MAX_LEVEL.sendMessage((Player) player);
                        return;
                    }
                    if (request >= maxLvl) request = maxLvl;

                    info.setLevel(request);
                    if (info.getXP() > info.getLevelXP()) info.setAbsoluteXP(0);
                    if (request > pre) {
                        boolean silent = false;
                        if (args.length > 3 && args[3].equalsIgnoreCase("silent")) silent = true;

                        if (!silent) {
                            int currentLvl = info.getLevel();
                            LevelUp level = null;

                            for (int lvl = 0; lvl < currentLvl; lvl++) {
                                level = LevelUp.getProperties(request).evaluate(info, lvl);
                                level.perform(info, "%next_maxxp%", info.getLevelXP(request));
                            }

                            if (level != null && player.isOnline()) level.celebrate((Player) player, plugin, request);
                        }
                    }

                    PlayerDataManager.addLevel(player, request);
                    HandleSimpleSetters.handleSuccess(sender, SkillsLang.COMMAND_USER_LEVEL_SUCCESS, player, amount, info.getLevel());
                    if (player.isOnline()) HealthAndEnergyManager.updateStats((Player) player);
                } catch (NumberFormatException e) {
                    SkillsCommandHandler.sendNotNumber(sender, "Level", args[2]);
                }
            } else {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "user level <player> <add/decrease/set> <amount>");
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return null;
        if (args.length == 2) return HandleSimpleSetters.tabComplete(args[1]);
        if (args.length == 3) return new String[]{"<amount>", "[silent]"};
        if (args.length == 4) return new String[]{"silent"};
        return new String[0];
    }
}
