package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.KeyBindingException;
import org.skills.abilities.Ability;
import org.skills.abilities.ActiveAbility;
import org.skills.abilities.KeyBinding;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.PlayerAbilityData;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.types.Skill;

import java.util.Map;
import java.util.Objects;

public class CommandKeyBinding extends SkillsCommand {
    public CommandKeyBinding() {
        super("bindings", SkillsLang.COMMAND_BINDINGS_DESCRIPTION, "key", "binding");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        Skill skill = info.getSkill();

        if (skill.isNone()) {
            SkillsLang.COMMAND_BINDINGS_NO_CLASS.sendMessage(sender);
            return;
        }

        if (args.length < 2) {
            SkillsLang.COMMAND_BINDINGS_USAGE.sendMessage(sender);
            return;
        }

        Ability ability = skill.getAbility(args[0].toLowerCase());
        if (ability == null) {
            SkillsLang.COMMAND_BINDINGS_UNKNOWN_ABILITY.sendMessage(player, "%ability%", args[0]);
            return;
        }
        if (ability.isPassive()) {
            SkillsLang.COMMAND_BINDINGS_NOT_ACTIVE_ABILITY.sendMessage(player, "%ability%", ability.getTitle(info));
            return;
        }

        Map<String, PlayerAbilityData> abilities = info.getAbilities();
        PlayerAbilityData abilityData = abilities.get(ability.getName());
        ActiveAbility activeAbility = (ActiveAbility) ability;
        KeyBinding[] binding;
        try {
            if (args[1].equals("~")) binding = activeAbility.getActivationKey(info);
            else binding = KeyBinding.parseBinding(args[1]);
        } catch (KeyBindingException ex) {
            SkillsLang.COMMAND_BINDINGS_INVALID.sendMessage(player, "%binding%", args[1]);
            return;
        }

        for (Map.Entry<String, PlayerAbilityData> currentAbility : abilities.entrySet()) {
            Ability ab = Objects.requireNonNull(skill.getAbility(currentAbility.getKey()),
                    () -> "Unknown ability " + currentAbility.getKey() + " for " + skill.getName());
            if (ab.isPassive()) continue;
            if (ability == ab) continue;
            ActiveAbility activeAbs = (ActiveAbility) ab;

            KeyBinding[] abilityBinding = currentAbility.getValue().getKeyBinding();
            if (abilityBinding == null) abilityBinding = activeAbs.getActivationKey(info);

            if (KeyBinding.isIllegalBinding(binding, abilityBinding)) {
                SkillsLang.COMMAND_BINDINGS_CONFLICT.sendMessage(player,
                        "%binding%", KeyBinding.toString(binding), "%other_binding%", KeyBinding.toString(abilityBinding),
                        "%ability%", ab.getName());
                return;
            }
        }

        abilityData.setKeyBinding(binding);
        SkillsLang.COMMAND_BINDINGS_CHANGED.sendMessage(player, "%binding%", KeyBinding.toString(binding), "%ability%", ability.getName());
    }

    @Override
    public @Nullable
    String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) return new String[0];
        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        if (args.length == 1) return info.getSkill().getAbilities()
                .stream()
                .filter(a -> !a.isPassive())
                .map(Ability::getName)
                .toArray(String[]::new);

        if (args.length == 2) {
            Ability ability = info.getSkill().getAbility(args[0]);
            if (ability == null) return new String[0];
            if (ability.isPassive()) return new String[0];
            ActiveAbility activeAbility = (ActiveAbility) ability;

            PlayerAbilityData data = info.getAbilityData(activeAbility);
            KeyBinding[] binding;

            if (data == null || data.getKeyBinding() == null) binding = activeAbility.getActivationKey(info);
            else binding = data.getKeyBinding();

            return new String[]{
                    "Current: " + KeyBinding.toString(binding),
                    "Default: " + KeyBinding.toString(activeAbility.getActivationKey(info)),
                    "Type '~' to reset to default."
            };
        }
        return new String[0];
    }
}
