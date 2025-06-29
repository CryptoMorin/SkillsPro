package org.skills.commands.general;

import com.cryptomorin.xseries.XWorldBorder;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.utils.time.TimeUtils;
import org.skills.commands.SkillsCommand;
import org.skills.main.locale.SkillsLang;

import java.time.Duration;
import java.util.Locale;

public class CommandWorldBorder extends SkillsCommand {
    public CommandWorldBorder() {
        super("worldborder", SkillsLang.COMMAND_WORLDBORDER_DESCRIPTION, false, "wb");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendMessage(sender);
            return;
        }
        if (args.length == 0) {
            SkillsLang.COMMAND_WORLDBORDER_USAGE.sendMessage(sender);
            return;
        }

        Player player = (Player) sender;
        XWorldBorder wb = XWorldBorder.getOrCreate(player);

        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "size":
                wb.setSize(Double.parseDouble(args[1]), Duration.ofMillis(TimeUtils.parseTime(args[2])));
                break;
            case "lerpsizetarget":
            case "lerpsize":
            case "lerp":
                wb.setSizeLerpTarget(Double.parseDouble(args[1]));
                break;
            case "center":
                Location pLoc = player.getLocation();
                wb.setCenter(
                        args[1].equals("~") ? pLoc.getX() : Double.parseDouble(args[1]),
                        args[2].equals("~") ? pLoc.getZ() : Double.parseDouble(args[2])
                );
                break;
            case "warningdistance":
                wb.setWarningDistance(Integer.parseInt(args[1]));
                break;
            case "warningtime":
                wb.setWarningTime(Duration.ofMillis(TimeUtils.parseTime(args[1])));
                break;
            case "remove":
                XWorldBorder.remove(player);
                break;
            case "info":
                break;
            default:
                SkillsLang.COMMAND_WORLDBORDER_USAGE.sendMessage(sender);
        }

        player.sendMessage(wb.toString());
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (args.length == 0 || args.length == 1) {
            return new String[]{"size", "lerpSizeTarget", "center", "warningDistance", "warningTime", "remove", "info"};
        } else {
            switch (args[0].toLowerCase(Locale.ENGLISH)) {
                case "size":
                    return new String[]{"<size> <duration>"};
                case "lerpsizetarget":
                    return new String[]{"<size>"};
                case "center":
                    return new String[]{"<x> <z>"};
                case "setwarningdistance":
                    return new String[]{"<distance in blocks>"};
                case "setwarningtime":
                    return new String[]{"<duration>"};
                case "remove":
                case "info":
                    return new String[0];
                default:
                    return new String[]{"Unknown"};
            }
        }
    }
}
