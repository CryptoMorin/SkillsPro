package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.HealthAndEnergyManager;
import org.skills.types.Stat;
import org.skills.utils.MathUtils;

public class CommandTest extends SkillsCommand {
    public CommandTest() {
        super("test", null, "&4A command to give some levels/souls/stats and etc for testing purposes.", false);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        info.addSouls(MathUtils.randInt(5000, 100000));
        info.setLevel(MathUtils.randInt(50, 100));
        info.setAbsoluteXP(info.getLevelXP() / 2);
        info.addStat(Stat.POINTS, MathUtils.randInt(100, 200));
        HealthAndEnergyManager.updateStats(player);
        MessageHandler.sendMessage(sender, "&2Added random stats.");
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return null;
    }
}
