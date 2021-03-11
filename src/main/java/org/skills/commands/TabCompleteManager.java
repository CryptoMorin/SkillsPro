package org.skills.commands;

import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.skills.masteries.managers.Mastery;
import org.skills.masteries.managers.MasteryManager;
import org.skills.types.SkillManager;
import org.skills.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TabCompleteManager implements TabCompleter {
    protected static @NonNull
    List<String> getSuggestions(CommandSender sender, @Nullable String starts) {
        return SkillsCommandHandler.getCommands().stream().filter(c -> c.getGroup() == null && c.hasPermission(sender))
                .map(SkillsCommand::getName).filter(o -> o.startsWith(starts.toLowerCase())).collect(Collectors.toList());
    }

    public static @NonNull
    List<String> getSubCommand(CommandSender sender, @NonNull SkillsCommand command, @NonNull String[] args) {
        String starts = args[0];
        SkillsCommand[] subCommands = command.getSubCommands();
        if (subCommands.length == 0) return new ArrayList<>();

        Stream<String> stream = Arrays.stream(subCommands).filter(c -> c.hasPermission(sender)).map(SkillsCommand::getName);
        if (starts.isEmpty()) return stream.collect(Collectors.toList());
        return stream.filter(s -> s.startsWith(starts.toLowerCase())).collect(Collectors.toList());
    }

    public static String[] getMasteries(String starts) {
        return Strings.isNullOrEmpty(starts) ? MasteryManager.getMasteries().stream().map(Mastery::getName).toArray(String[]::new)
                : MasteryManager.getMasteries().stream().map(Mastery::getName).filter(x
                -> x.toLowerCase().startsWith(starts.toLowerCase())).toArray(String[]::new);
    }

    public static List<String> getSkillTypes(String starts) {
        return Strings.isNullOrEmpty(starts) ? SkillManager.getSkills().keySet().stream().map(StringUtils::capitalize).collect(Collectors.toList())
                : SkillManager.getSkills().keySet().stream().filter(s -> s.startsWith(starts.toLowerCase())).map(StringUtils::capitalize).collect(Collectors.toList());
    }

    public static @NonNull
    String[] descendingSuggestions(@NonNull String[] suggestions, @NonNull String[] args) {
        if (args.length - 1 == -1) return suggestions;
        int skip = args.length - 1;

        String[] left = Arrays.stream(suggestions).skip(skip).toArray(String[]::new);
        if (left.length == 0) return new String[0];

        String trimmed = Arrays.toString(left);
        trimmed = trimmed.substring(1, trimmed.length() - 1);
        return new String[]{trimmed};
    }

    public static String[] getSuggestions(String[] items, String starts) {
        return Strings.isNullOrEmpty(starts) ? items : Arrays.stream(items).filter(x -> x.startsWith(starts)).toArray(String[]::new);
    }

    private @NonNull
    List<String> getPlayers(@Nullable String starts) {
        Stream<String> names = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName);
        return Strings.isNullOrEmpty(starts) ? names.collect(Collectors.toList())
                : names.filter(n -> n.toLowerCase().startsWith(starts.toLowerCase())).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, @NonNull String[] args) {
        if (args.length == 1) return getSuggestions(sender, args[0]);

        SkillsCommandHandler.Pair<SkillsCommand, Integer> pair = SkillsCommandHandler.getLastCommand(args);
        SkillsCommand scmd = pair.getKey();
        if (scmd == null) return new ArrayList<>();
        int cmdIndex = pair.getValue();

        if (!scmd.hasPermission(sender)) return new ArrayList<>();
        String[] passable = Arrays.stream(args).skip(cmdIndex + 1).toArray(String[]::new);

        if (scmd.isGroup() && args.length == cmdIndex)
            return getSubCommand(sender, scmd, passable);

        String[] tabs = scmd.tabComplete(sender, passable);
        if (tabs == null) {
            if (passable.length != 0) return getPlayers(passable[0]);
            return getPlayers(null);
        }
        return Arrays.asList(tabs);
    }
}