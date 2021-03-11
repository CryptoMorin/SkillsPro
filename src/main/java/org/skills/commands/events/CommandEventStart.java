package org.skills.commands.events;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;
import org.skills.events.SkillsEvent;
import org.skills.events.SkillsEventManager;
import org.skills.events.SkillsEventType;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.MathUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class CommandEventStart extends SkillsCommand {
    public CommandEventStart(SkillsCommand group) {
        super("start", group, SkillsLang.COMMAND_EVENT_START_DESCRIPTION, false);
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (args.length >= 3) {
            SkillsEventType type = SkillsEventType.fromString(args[0]);
            if (type == null) {
                SkillsCommandHandler.sendUsage(sender, "<exp/soul> <time in mins> <multiplier>");
                return;
            }

            Long time = MathUtils.calcMillis(args[1], TimeUnit.SECONDS);
            if (time == null) {
                SkillsCommandHandler.sendUsage(sender, "<exp/soul> <time> <multiplier>");
                return;
            }
            double multiplier;

            try {
                multiplier = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                SkillsCommandHandler.sendUsage(sender, "<xp/soul> <time> <multiplier>");
                return;
            }

            SkillsEvent event = SkillsEventManager.getEvent(type);
            if (event == null || !event.isActive()) {
                event = new SkillsEvent(null, type, String.valueOf(multiplier), time);
                event.startEvent();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    SkillsLang.COMMAND_BOOST_BROADCAST.sendMessage(player,
                            "%type%", type.toString(),
                            "%multiplier%", String.valueOf(multiplier),
                            "%time%", event.getDisplayTime());
                }
            } else {
                SkillsLang.COMMAND_BOOST_ALREADY_STARTED.sendMessage(sender,
                        "%type%", type.toString(),
                        "%multiplier%", multiplier,
                        "%time%", event.getDisplayTime());
            }
        } else {
            SkillsCommandHandler.sendUsage(sender, "<xp/soul> <time> <multiplier>");
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) return new String[]{"Soul", "XP"};
        String[] suggestions = {"<time>", "<multiplier>"};
        return TabCompleteManager.descendingSuggestions(suggestions, Arrays.stream(args).skip(1).toArray(String[]::new));
    }
}
