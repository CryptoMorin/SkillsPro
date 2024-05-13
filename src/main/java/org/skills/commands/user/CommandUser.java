package org.skills.commands.user;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.commands.TabCompleteManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.HealthAndEnergyManager;
import org.skills.types.SkillScaling;

import java.util.Collection;
import java.util.Collections;

public class CommandUser extends SkillsCommand {
    public CommandUser() {
        super("user", SkillsLang.COMMAND_USER_DESCRIPTION, true, "playerinfo");

        new CommandUserXP(this);
        new CommandUserLevel(this);
        new CommandUserEnergy(this);
        new CommandUserCooldown(this);
        new CommandUserSouls(this);

        new CommandUserSkill(this);
        new CommandUserStats(this);
        new CommandUserReset(this);

        new CommandUserMastery(this);
        new CommandUserImprove(this);
        new CommandUserCosmetic(this);
        new CommandUserVulnerable(this);
    }

    public static void magicCircle(int tier, double size, ParticleDisplay display) {
        if (tier == 1) {
            // https://i.pinimg.com/originals/aa/ee/0a/aaee0a69680c1cd8b6d30d1814028143.jpg
            Particles.polygon(4, 4, size, 0.02, 0.3, display);
            Particles.polygon(4, 3, size / (size - 1), 0.5, 0, display.clone().rotate(0, Math.PI / 2, 0));
            Particles.polygon(8, 3, size / (size - 1), 0.5, 0, display);
            Particles.polygon(8, 3, size / (size - 1), 0.5, 0, display);
        } else if (tier == 2) {
            // https://i.pinimg.com/236x/41/bf/2d/41bf2d9769fc135039049c1f72bd011b--magic-circle-fantasy-weapons.jpg
            Particles.polygon(3, 3, size / (size - 1), 0.5, 0, display);
            Particles.polygon(6, 3, size / (size - 1), 0.5, 0, display);
        } else if (tier == 3) {
            // https://vignette.wikia.nocookie.net/overlordmaruyama/images/3/38/Overlord_EP04_023.png/revision/latest/scale-to-width-down/340?cb=20150730120703
        } else if (tier == 4) {
            // https://thumbs.dreamstime.com/b/sacred-geometry-magic-circle-rune-simple-sacred-geometry-magic-circle-rune-star-107887158.jpg
            Particles.polygon(8, 3, size / (size - 1), 0.5, 0, display);
            Particles.circle(size, size * 5, display);
        }

        Particles.circle(size, size * 10, display);
    }

    public static void handle(SkillsCommand cmd, @NotNull CommandSender sender, @NotNull String[] args, UserAmountHandler handler) {
        handle(cmd, sender, args, null,
                (changeFactory, player, info, type, silent) -> handler.handle(changeFactory, player, info, silent));
    }

    public static void handle(SkillsCommand cmd, @NotNull CommandSender sender, @NotNull String[] args,
                              String type, TypedUserAmountHandler handler) {
        boolean hasType = type != null;
        if (args.length < (hasType ? 4 : 3)) {
            String typeStr = hasType ? "" : " <" + type + '>';
            SkillsCommandHandler.sendUsage(sender, "user " + cmd.getName() + " <player> <add/decrease/set>" + typeStr + " <amount>");
            return;
        }

        Collection<? extends OfflinePlayer> players;
        if (args[0].equals("*")) players = Bukkit.getOnlinePlayers();
        else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

            if (!player.hasPlayedBefore()) {
                SkillsLang.PLAYER_NOT_FOUND.sendMessage(sender, "%name%", args[0]);
                return;
            }

            players = Collections.singletonList(player);
        }

        int silentIndex = hasType ? 4 : 3;
        boolean silent = args.length > (silentIndex + 1) && args[silentIndex].equalsIgnoreCase("silent");
        AmountChangeFactory changeFactory = AmountChangeFactory.of(sender, 1, (hasType ? 3 : 2), args);
        if (changeFactory == null) return;

        String fetchedType = null;
        if (hasType) fetchedType = args[2];

        for (OfflinePlayer player : players) {
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
            if (handler.handle(changeFactory, player, info, fetchedType, silent)) {
                changeFactory.handleSuccess(sender, "COMMAND_USER_" + cmd.getName().toUpperCase(), player);
                if (player.isOnline()) {
                    double maxEnergy = info.getScaling(SkillScaling.MAX_ENERGY);
                    if (info.getEnergy() > maxEnergy) info.setEnergy(maxEnergy);
                    HealthAndEnergyManager.updateStats(player.getPlayer());
                }
            }
        }
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        SkillsCommandHandler.executeHelperForGroup(this, sender);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return args.length < 2 ? TabCompleteManager.getSubCommand(sender, this, args).toArray(new String[0]) : new String[0];
    }

    @FunctionalInterface
    protected interface UserAmountHandler {
        boolean handle(AmountChangeFactory changeFactory, OfflinePlayer player, SkilledPlayer info, boolean silent);
    }

    @FunctionalInterface
    protected interface TypedUserAmountHandler {
        boolean handle(AmountChangeFactory changeFactory, OfflinePlayer player, SkilledPlayer info, String type, boolean silent);
    }
}
