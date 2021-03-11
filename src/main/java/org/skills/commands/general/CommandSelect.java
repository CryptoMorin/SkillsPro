package org.skills.commands.general;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;
import org.skills.commands.SkillsCommand;
import org.skills.commands.TabCompleteManager;
import org.skills.data.managers.PlayerSkill;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.HealthAndEnergyManager;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.Skill;
import org.skills.types.SkillManager;
import org.skills.types.SkillScaling;

import java.util.List;
import java.util.Map;

public class CommandSelect extends SkillsCommand {
    public CommandSelect() {
        super("select", SkillsLang.COMMAND_SELECT_DESCRIPTION, false, "selectskill", "skillselect", "change");
    }

    public static void select(Player player, SkilledPlayer info, Skill skill) {
        if (info.getSkill().equals(skill)) {
            SkillsLang.COMMAND_SELECT_ALREADY_CHOSEN.sendMessage(player, "%skill%", skill.getName());
            XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
            return;
        }

        if (!player.hasPermission("skills.select." + skill.getName().toLowerCase())) {
            SkillsLang.SKILL_NO_PERMISSION.sendMessage(player, "%skill%", skill.getName());
            XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
            return;
        }

        if (info.hasSkill() && !player.hasPermission("skills.change")) {
            SkillsLang.SKILL_OWNED_ERROR.sendMessage(player);
            XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
            return;
        }

        if (!skill.isNone()) {
            int requiredLevel = (int) skill.getScaling(info, SkillScaling.REQUIRED_LEVEL);
            if (info.getLevel() < requiredLevel) {
                SkillsLang.SKILL_REQUIRED_LEVEL.sendMessage(player, "%level%", requiredLevel);
                XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                return;
            }
        }

        if (!info.canChangeSkill() && !player.hasPermission("skills.change.cooldownbypass")) {
            SkillsLang.SKILL_CHANGE_COOLDOWN_ERROR.sendMessage(player,
                    "%cooldown%", info.getTimeLeftToChangeSkillString());

            XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
            return;
        }

        int cost = 0;
        if (!info.hasSkill(skill) || !SkillsConfig.BUY_CLASS_ONCE.getBoolean()) {
            cost = (int) skill.getScaling(info, SkillScaling.COST);
            if (info.getSouls() < cost) {
                SkillsLang.SKILL_SELECT_COST.sendMessage(player, "%cost%", cost);
                return;
            }
        }

        boolean changed = info.hasSkill();
        if (cost != 0) info.addSouls(-cost);
        info.setActiveSkill(skill);
        info.setEnergy(0);
        performCommands(info, changed);

        if (SkillsConfig.STARTER_ENABLED.getBoolean()) {
            int lvl = SkillsConfig.STARTER_ABILITIES_LEVEL.getInt();
            if (lvl > 0) {
                for (Ability ability : info.getSkill().getAbilities()) {
                    info.setAbilityLevel(ability, lvl);
                }
            }
        }

        if (changed) {
            if (SkillsConfig.SKILL_CHANGE_RESET_LEVEL.getBoolean()) {
                info.setAbsoluteXP(0);
                info.setLevel(0);
            }
            if (SkillsConfig.SKILL_CHANGE_RESET_SOULS.getBoolean()) info.setSouls(0);
            if (SkillsConfig.SKILL_CHANGE_RESET_ABILITIES.getBoolean()) {
                info.getImprovements().clear();
                info.getDisabledAbilities().clear();
            }
            if (SkillsConfig.SKILL_CHANGE_RESET_STATS.getBoolean()) info.getStats().clear();
            if (SkillsConfig.SKILL_CHANGE_RESET_MASTERIES.getBoolean()) info.getMasteries().clear();
        }
        SkillsLang.SKILL_SELECTED.sendMessage(player);
        HealthAndEnergyManager.updateStats(player);
    }

    public static void performCommands(SkilledPlayer info, boolean changed) {
        Player player = info.getPlayer();
        FileConfiguration config = info.getSkill().getAdapter().getConfig();
        List<String> commands = changed ? config.getStringList("commands-to-perform-upon-change") :
                config.getStringList("commands-to-perform-upon-select");
        for (String command : commands) {
            CommandSender executor = command.toUpperCase().startsWith("CONSOLE:") ? Bukkit.getConsoleSender() : player;

            int index = command.indexOf(':'); // 58 = unicode code point
            if (index != -1) command = command.substring(index + 1);
            Bukkit.dispatchCommand(executor, ServiceHandler.translatePlaceholders(player, command));
        }
    }

    public static void openMenu(Player player, SkilledPlayer info) {
        InteractiveGUI gui = GUIParser.parseOption(player, "selector");
        for (Map.Entry<String, Skill> skills : SkillManager.getSkills().entrySet()) {
            Skill skill = skills.getValue();
            if (skill.isNone()) continue;
            PlayerSkill playerSkill = info.getSkills().get(skill.getName());

            gui.push(skills.getKey(), () -> {
                        player.closeInventory();
                        select(player, info, skill);
                    },
                    "%cost%", (int) skill.getScaling(info, SkillScaling.COST),
                    "%required-level%", (int) skill.getScaling(info, SkillScaling.REQUIRED_LEVEL),
                    "%level%", playerSkill == null ? 0 : playerSkill.getLevel());
        }

        gui.setRest();
        gui.openInventory(player);
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        Skill skill = null;
        if (args.length > 0) skill = SkillManager.getSkill(args[0]);
        if (skill == null) {
            openMenu(player, info);
            return;
        }

        select(player, info, skill);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length > 1) return new String[0];
        return TabCompleteManager.getSkillTypes(args[0]).toArray(new String[0]);
    }
}
