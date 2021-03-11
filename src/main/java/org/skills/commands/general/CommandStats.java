package org.skills.commands.general;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.types.Stat;

import java.util.ArrayList;
import java.util.List;

public class CommandStats extends SkillsCommand {
    public CommandStats() {
        super("stats", SkillsLang.COMMAND_STATS_DESCRIPTION, false, "stat", "statpoints", "statspoint");
    }

    private static void openMenu(Player player, SkilledPlayer of, boolean refresh) {
        InteractiveGUI gui = GUIParser.parseOption(player, "stats");

        for (Stat stat : Stat.STATS.values()) {
            String name = stat.getNode();
            Object[] edits = {"%max-level%", stat.getMaxLevel(), "%color%", stat.getColor(), "%level%", of.getStat(stat)};

            gui.push(name + "-add", edits, () -> {
                addToStat(player, of, stat);
                openMenu(player, of, true);
            });
            gui.push(name + "-add-all", edits, () -> {
                addAllPoints(player, of, stat);
                openMenu(player, of, true);
            });
        }

        gui.push("distribute", () -> {
            distributeStats(player, of);
            openMenu(player, of, true);
        });
        gui.push("reset", () -> reset(player, of));

        gui.setRest();
        gui.openInventory(player, refresh);
    }

    public static void distributeStats(Player viewer, SkilledPlayer info) {
        Player player = info.getPlayer();
        int points = info.getPoints();
        if (points < 2) {
            SkillsLang.GUIS_STATMENU_STATPOINTS_NEEDED.sendMessage(player);
            return;
        }

        List<Stat> stats = info.getSkill().getStats();
        if (stats == null) {
            stats = new ArrayList<>(Stat.STATS.values());
            stats.removeIf(x -> Stat.isPoints(x.getDataNode()));
        }
        int distStat = points / stats.size();
        int cost = 0;

        for (Stat stat : stats) {
            int spent, statAmt = info.getStat(stat);

            if (statAmt + distStat <= stat.getMaxLevel()) {
                info.setStat(stat, statAmt + distStat);
                spent = distStat;
            } else {
                spent = stat.getMaxLevel() - statAmt;
                info.setStat(stat, stat.getMaxLevel());
            }

            cost += spent;
            SkillsLang.GUIS_STATMENU_STATPOINTS_DISTRIBUTION.sendMessage(player, "%amount%", spent, "%stat%", stat.getName());
        }

        info.setStat(Stat.POINTS, points - cost);
        SkillsLang.GUIS_STATMENU_STATPOINTS_SPENT.parse(player, "%amount%", cost);
    }

    public static void addAllPoints(Player viewer, SkilledPlayer info, Stat type) {
        int points = info.getPoints();
        if (points < 1) {
            SkillsLang.NOT_ENOUGH_STATPOINTS.sendMessage(info.getPlayer());
            return;
        }

        int maxStat = type.getMaxLevel();
        int stat = info.getStat(type);
        int toAdd = points;
        if (stat + toAdd > maxStat) toAdd = maxStat - stat;
        // 12 -> 300 => 312 > 100 => 312 - 100 -> 212
        int left = points - toAdd;

        info.addStat(type.getDataNode(), toAdd);
        info.setStat(Stat.POINTS, left);
    }

    private static void addToStat(Player viewer, SkilledPlayer info, Stat type) {
        int points = info.getPoints();
        if (points < 1) {
            SkillsLang.NOT_ENOUGH_STATPOINTS.sendMessage(info.getPlayer());
            return;
        }

        int stat = info.getStat(type.getDataNode());
        if (stat >= type.getMaxLevel()) return;

        info.setStat(type.getDataNode(), stat + 1);
        info.setStat(Stat.POINTS, points - 1);
    }

    public static void reset(Player viewer, SkilledPlayer info) {
        Player player = info.getPlayer();
        int cost = SkillsConfig.STATS_RESET_COST.getInt();
        long souls = info.getSouls();
        long diff = souls - cost;

        if (diff < 0) {
            SkillsLang.NOT_ENOUGH_SOULS.sendMessage(player);
            return;
        }

        InteractiveGUI gui = GUIParser.parseOption(player, "stats-reset-confirmation");
        gui.push("reset", () -> {
            info.setSouls(diff);
            info.resetStats();

            SkillsLang.GUIS_STATMENU_RESET_MESSAGE.sendMessage(viewer, player);
            openMenu(viewer, info, true);
        });
        gui.push("cancel", () -> openMenu(viewer, info, true));

        gui.setRest();
        gui.openInventory(player);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (args.length != 0) {
                if (sender.hasPermission("skills.command.stats.others")) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                    if (player == null || !player.hasPlayedBefore()) {
                        SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
                        return;
                    }
                    openMenu((Player) sender, SkilledPlayer.getSkilledPlayer(player), false);
                } else {
                    SkillsLang.COMMAND_STATS_OTHERS_PERMISSION.sendMessage(sender);
                }
                return;
            }
            openMenu((Player) sender, SkilledPlayer.getSkilledPlayer((Player) sender), false);
        } else {
            SkillsLang.PLAYERS_ONLY.sendMessage(sender);
        }
    }

    @Override
    public String[] tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player && sender.hasPermission("skills.command.stats.others")) return null;
        return new String[0];
    }
}
