package org.skills.commands.general;

import com.cryptomorin.xseries.messages.Titles;
import com.cryptomorin.xseries.XSound;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.HealthAndEnergyManager;
import org.skills.services.ServiceVault;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.SkillScaling;
import org.skills.types.Stat;
import org.skills.utils.MathUtils;
import org.skills.utils.StringUtils;

public class CommandPrestige extends SkillsCommand {
    public CommandPrestige() {
        super("prestige", SkillsLang.COMMAND_PRESTIGE_DESCRIPTION, "rebirth");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendMessage(sender);
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        if (!info.hasSkill()) {
            SkillsLang.NO_SKILL.sendMessage(player);
            return;
        }

        int maxPrestige = info.getMaxPrestige();
        if (info.getPrestige() >= maxPrestige) {
            SkillsLang.SKILL_PRESTIGE_MAXED.sendMessage(player, "%max%", maxPrestige);
            XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
            return;
        }

        int maxLevel = (int) info.getScaling(SkillScaling.MAX_LEVEL);
        if (info.getLevel() < maxLevel) {
            SkillsLang.SKILL_PRESTIGE_MAX_LEVEL_REQUIRED.sendMessage(player, "%level%", maxLevel);
            XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
            return;
        }

        int currentPrestige = info.getPrestige();
        int nextPrestige = currentPrestige + 1;
        ConfigurationSection prestigeSection = getPrestigeSection(nextPrestige);

        if (SkillsConfig.DEBUG.getBoolean()) {
            MessageHandler.sendConsolePluginMessage("&8[Skills Debug] Prestige request -> current=" + currentPrestige
                    + ", next=" + nextPrestige
                    + ", section=" + (prestigeSection == null ? "null" : prestigeSection.getCurrentPath())
                    + ", statpoints=" + (prestigeSection == null ? "null" : prestigeSection.getString("statpoints"))
                    + ", has-title="
                    + (prestigeSection != null && prestigeSection.getConfigurationSection("title") != null));
        }

        int soulsCost = Math.max(0, getSoulsCost(prestigeSection, player, nextPrestige));
        double moneyCost = Math.max(0D, getMoneyCost(prestigeSection, player, nextPrestige));

        if (soulsCost > 0 && info.getSouls() < soulsCost) {
            SkillsLang.COMMAND_SOULS_NOT_ENOUGH_SOULS.sendMessage(player, "%amount%", soulsCost);
            XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
            return;
        }
        if (moneyCost > 0D) {
            if (!ServiceHandler.isAvailable("Vault")) {
                SkillsLang.COMMAND_SOULS_SERVICE_UNAVAILABLE.sendMessage(player);
                XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
                return;
            }
            if (!ServiceVault.hasMoney(player, moneyCost)) {
                SkillsLang.COMMAND_SOULS_NOT_ENOUGH_MONEY.sendMessage(player, "%amount%", moneyCost);
                XSound.BLOCK_NOTE_BLOCK_BASS.record().soundPlayer().forPlayers(player).play();
                return;
            }
        }

        if (soulsCost > 0) info.addSouls(-soulsCost);
        if (moneyCost > 0D) ServiceVault.withdraw(player, moneyCost);

        int gainedStatPoints = getStatPoints(prestigeSection, player, nextPrestige);

        info.addPrestige(1);
        if (SkillsConfig.PRESTIGE_RESET_LEVEL.getBoolean())
            info.setLevel(0);
        if (SkillsConfig.PRESTIGE_RESET_XP.getBoolean())
            info.setAbsoluteXP(0);
        if (SkillsConfig.PRESTIGE_RESET_SOULS.getBoolean())
            info.setSouls(0);
        if (SkillsConfig.PRESTIGE_RESET_STATS.getBoolean())
            info.getStats().clear();
        if (SkillsConfig.PRESTIGE_RESET_ABILITIES.getBoolean())
            info.getAbilities().clear();
        if (SkillsConfig.PRESTIGE_RESET_MASTERIES.getBoolean())
            info.getMasteries().clear();
        if (SkillsConfig.PRESTIGE_RESET_ENERGY.getBoolean())
            info.setEnergy(0);

        if (SkillsConfig.DEBUG.getBoolean()) {
            MessageHandler
                    .sendConsolePluginMessage("&8[Skills Debug] Prestige after reset -> points=" + info.getPoints());
        }

        if (gainedStatPoints > 0) info.addStat(Stat.POINTS, gainedStatPoints);

        if (SkillsConfig.DEBUG.getBoolean()) {
            MessageHandler
                    .sendConsolePluginMessage("&8[Skills Debug] Prestige after award -> gained=" + gainedStatPoints
                            + ", points=" + info.getPoints());
        }

        Object[] edits = getMessageEdits(info, currentPrestige, soulsCost, moneyCost, gainedStatPoints);
        performCommands(prestigeSection, player, edits);
        performMessages(prestigeSection, player, edits, info);

        HealthAndEnergyManager.updateStats(player);
        XSound.UI_TOAST_CHALLENGE_COMPLETE.record().soundPlayer().forPlayers(player).play();
    }

    private static ConfigurationSection getPrestigeSection(int prestige) {
        ConfigurationSection section = SkillsConfig.PRESTIGE_LEVELS.getSection();
        if (section == null) return null;
        int closest = SkillsConfig.getClosestLevelSection(section, prestige);
        return section.getConfigurationSection(String.valueOf(closest));
    }

    private static int getSoulsCost(ConfigurationSection section, Player player, int prestige) {
        if (section == null || !section.isSet("costs.souls")) return 0;
        return evaluateInt(section.getString("costs.souls"), player, prestige);
    }

    private static double getMoneyCost(ConfigurationSection section, Player player, int prestige) {
        if (section == null || !section.isSet("costs.money")) return 0D;
        return evaluateDouble(section.getString("costs.money"), player, prestige);
    }

    private static int getStatPoints(ConfigurationSection section, Player player, int prestige) {
        if (section == null || !section.isSet("statpoints")) return 0;
        return Math.max(0, evaluateInt(section.getString("statpoints"), player, prestige));
    }

    private static int evaluateInt(String expression, Player player, int prestige) {
        return (int) Math.round(evaluateDouble(expression, player, prestige));
    }

    private static double evaluateDouble(String expression, Player player, int prestige) {
        if (Strings.isNullOrEmpty(expression)) return 0D;
        String parsed = ServiceHandler.translatePlaceholders(player, expression)
                .replace("prstge", String.valueOf(prestige))
                .replace("lvl", String.valueOf(prestige));
        return MathUtils.evaluateEquation(parsed);
    }

    private static Object[] getMessageEdits(SkilledPlayer info, int previousPrestige, int soulsCost, double moneyCost, int gainedStatPoints) {
        int prestige = info.getPrestige();
        return new Object[]{
                "%prestige%", prestige,
                "%previous_prestige%", previousPrestige,
                "%next_prestige%", prestige + 1,
                "%prestige_display%", info.getPrestigeDisplay(),
                "%display%", info.getPrestigeDisplay(),
                "%gained_stats%", StringUtils.toFancyNumber(gainedStatPoints),
                "%cost_souls%", StringUtils.toFancyNumber(soulsCost),
                "%cost_money%", StringUtils.toFancyNumber(moneyCost)
        };
    }

    private static void performCommands(ConfigurationSection section, Player player, Object... edits) {
        if (section == null) return;
        for (String command : section.getStringList("commands")) {
            CommandSender executor = command.toUpperCase().startsWith("CONSOLE:") ? Bukkit.getConsoleSender() : player;

            int index = command.indexOf(':');
            String cmd = index == -1 ? command : command.substring(index + 1);
            cmd = MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, cmd), edits);
            Bukkit.dispatchCommand(executor, cmd);
        }
    }

    private static void performMessages(ConfigurationSection section, Player player, Object[] edits, SkilledPlayer info) {
        if (section != null) {
            String message = section.getString("message");
            if (!Strings.isNullOrEmpty(message)) {
                player.sendMessage(MessageHandler.colorize(MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, message), edits)));
            }

            ConfigurationSection titleSection = section.getConfigurationSection("title");
            if (titleSection != null) {
                String title = titleSection.getString("title");
                if (!Strings.isNullOrEmpty(title))
                    title = MessageHandler.colorize(MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, title), edits));

                String subtitle = titleSection.getString("subtitle");
                if (!Strings.isNullOrEmpty(subtitle))
                    subtitle = MessageHandler.colorize(MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, subtitle), edits));

                int fadeIn = titleSection.getInt("fade-in");
                int stay = titleSection.getInt("stay");
                int fadeOut = titleSection.getInt("fade-out");

                try {
                    Titles.sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
                } catch (Throwable ex) {
                    player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                }
            }
        }

        SkillsLang.SKILL_PRESTIGED.sendMessage(player,
                "%prestige%", info.getPrestige(),
                "%display%", info.getPrestigeDisplay());
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
