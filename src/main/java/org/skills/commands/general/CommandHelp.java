package org.skills.commands.general;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.HoverLang;
import org.skills.main.locale.SkillsLang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp extends SkillsCommand {
    public CommandHelp() {
        super("help", SkillsLang.COMMAND_HELP_DESCRIPTION, false, "h", "?");
    }

    public static int getPageNumbers(List<SkillsCommand> commands) {
        return commands.size() % 5 != 0 ? commands.size() / 5 + 1 : commands.size() / 5;
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        int page = 0;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]) - 1;
                if (page + 1 < 1) {
                    SkillsLang.COMMAND_HELP_NEGATIVE_PAGES.sendMessage(sender);
                    return;
                }
            } catch (NumberFormatException e) {
                sendUsage(sender, SkillsLang.COMMAND_HELP_USAGE);
                return;
            }
        }

        List<SkillsCommand> commands = Arrays.asList(SkillsCommandHandler.getCommands()
                .stream().filter(c -> c.hasPermission(sender)).toArray(SkillsCommand[]::new));

        int eachPage = SkillsConfig.COMMANDS_EACH_PAGE.getInt();
        List<SkillsCommand> selectedCmds = commands.stream().skip((long) page * eachPage).collect(Collectors.toList());
        if (selectedCmds.isEmpty()) {
            return;
        }

        int maxPages = getPageNumbers(commands);
        SkillsLang.COMMAND_HELP_HEADER.sendMessage(sender, "%previous_page%", page, "%page%", page + 1, "%next_page%", page + 2, "%max_pages%", maxPages);
        selectedCmds.stream().limit(eachPage).forEach((cmd) -> {
            StringBuilder name = new StringBuilder(cmd.getName());
            SkillsCommand group = cmd.getGroup();

            while (group != null) {
                name.insert(0, group.getName() + ' ');
                group = group.getGroup();
            }

            SkillsLang.COMMAND_HELP_COMMANDS.sendMessage(sender, "%cmd%", name.toString(), "%description%", cmd.getDescription());
        });

        HoverLang.ComplexComponent builder = new HoverLang.ComplexComponent(SkillsLang.COMMAND_HELP_FOOTER_PAGE.getLang());
        OfflinePlayer placeholder = sender instanceof Player ? (OfflinePlayer) sender : null;
        for (int pages = 1; pages <= getPageNumbers(commands); pages++) {
            if (pages == page + 1)
                builder.append(SkillsLang.COMMAND_HELP_FOOTER_CURRENT_PAGE.getLang(), placeholder, "%number%", pages, "%previous_page%", page, "%page%", page + 1,
                        "%next_page%", page + 2, "%max_pages%", maxPages);
            else builder.append(placeholder, "%number%", pages, "%previous_page%", page, "%page%", page + 1, "%next_page%", page + 2, "%max_pages%", maxPages);
            builder.append(" ");
        }
        SkillsLang.COMMAND_HELP_FOOTER.sendMessage(sender, builder.asComplexEdit("%pages%"),
                "%previous_page%", page, "%page%", page + 1, "%next_page%", page + 2, "%max_pages%", maxPages);


//
//
//        int page = 0;
//        if (args.length >= 1) {
//            try {
//                page = Integer.parseInt(args[0]) - 1;
//                if (page + 1 < 1) {
//                    SkillsLang.COMMAND_HELP_NO_HIDDEN_PAGES.sendMessage(sender);
//                    return;
//                }
//            } catch (NumberFormatException e) {
//                SkillsCommandHandler.sendUsage(sender, "help <number>");
//                return;
//            }
//        }
//
//        List<SkillsCommand> commands = Arrays.asList(SkillsCommandHandler.getCommands()
//                .stream().filter(c -> c.hasPermission(sender)).toArray(SkillsCommand[]::new));
//
//        int eachPage = SkillsConfig.COMMANDS_EACH_PAGE.getInt();
//        List<SkillsCommand> selectedCmds = commands.stream().skip(page * eachPage).collect(Collectors.toList());
//        if (selectedCmds.size() < 1) {
//            SkillsLang.COMMAND_HELP_NO_MORE_PAGES.sendMessage(sender);
//            return;
//        }
//
//        SkillsLang.COMMAND_HELP_PAGES_HEADER.sendMessage(sender, "%page%", page + 1, "%maxpages%", getPageNumbers(commands));
//        selectedCmds.stream().limit(eachPage).forEach((cmd) -> {
//            StringBuilder name = new StringBuilder(cmd.getName());
//            SkillsCommand group = cmd.getGroup();
//
//            while (group != null) {
//                name.insert(0, group.getName() + " ");
//                group = group.getGroup();
//            }
//
//            SkillsLang.COMMAND_HELP_PAGES_COMMANDS.sendMessage(sender, "%cmd%", name.toString(), "%description%", cmd.getDescription());
//        });
//        if (selectedCmds.stream().skip(eachPage).count() > 1) SkillsLang.COMMAND_HELP_NEXTPAGE.sendMessage(sender, "%page%", page + 2);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        List<String> nums = new ArrayList<>();
        List<SkillsCommand> commands = Arrays.asList(SkillsCommandHandler.getCommands()
                .stream().filter(c -> c.hasPermission(sender)).toArray(SkillsCommand[]::new));

        for (int pages = getPageNumbers(commands); pages > 0; pages--) nums.add(String.valueOf(pages));
        return nums.toArray(new String[0]);
    }
}