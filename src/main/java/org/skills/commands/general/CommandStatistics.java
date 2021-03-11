package org.skills.commands.general;

import com.google.common.base.Strings;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.HoverLang;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.MathUtils;
import org.skills.utils.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CommandStatistics extends SkillsCommand {
    public CommandStatistics() {
        super("statistics", null, SkillsLang.COMMAND_STATISTICS_DESCRIPTION, false, "statistic", "percents");
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        CompletableFuture.runAsync(() -> {
            MessageHandler.sendMessage(sender, "&3Loading players data. This can take a few seconds...");

            Map<String, Integer> statistics = new HashMap<>();
            int loaded = 0;

            for (SkilledPlayer info : plugin.getPlayerDataManager().getAllData()) {
                Objects.requireNonNull(info.getSkill(), () -> "Player skill cannot be null while handling statistics for: " + info.getOfflinePlayer().getName());
                String displayName = info.getSkill().getDisplayName();
                Objects.requireNonNull(displayName, () -> "Player skill displayname cannot be null while handling statistics for: " + info.getOfflinePlayer().getName());

                statistics.put(displayName, statistics.getOrDefault(displayName, 0) + 1);
                loaded++;
            }

            int bars = 100;
            MessageHandler.sendMessage(sender, "&3Loaded &e" + loaded + " &3player data.");
            statistics = sortByValue(statistics);
            for (Map.Entry<String, Integer> stats : statistics.entrySet()) {
                double percent = MathUtils.getPercent(stats.getValue(), loaded);
                int on = (int) Math.floor(MathUtils.getAmountFromAmount(percent, 100, bars));
                int off = bars - on;
                String displayPercent = "&2" + Strings.repeat("|", on) + (off != 0 ? "&8" + Strings.repeat("|", off) : "");

                percent = MathUtils.roundToDigits(percent, 3);
                HoverLang.sendComplexMessage(sender, null, "COMPLEX:&3" + stats.getKey() + "&8: hover:{" + displayPercent + ",&e" + percent + "%} " +
                        "&8(&7" + StringUtils.toFancyNumber(stats.getValue()) + "&8)");
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        return new String[0];
    }
}
