package org.skills.masteries.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.SkillsMasteryConfig;
import org.skills.main.locale.SkillsLang;

public class MasteryGUIManager {
    public static void setMastery(SkilledPlayer info, Player player, Mastery mastery, boolean upgrade) {
        int lvl = info.getMasteryLevel(mastery);
        if (upgrade) {
            if (lvl >= mastery.getMaxLevel()) {
                SkillsLang.MASTERY_MAXIMUM_LEVEL.sendMessage(player);
                return;
            }

            int requiredLvl = mastery.getRequiredLevel(info);
            if (info.getLevel() < requiredLvl) {
                SkillsLang.MASTERY_REQUIRED_LEVEL.sendMessage(player, "%level%", requiredLvl);
                return;
            }

            int cost = mastery.getUpgradeCost(info);
            if (info.getSouls() >= cost) {
                info.addSouls(-cost);
                info.addMasteryLevel(mastery, 1);
                SkillsLang.MASTERY_UPGRADED.sendMessage(player, "%mastery%", mastery.getDisplayName(), "%cost%", cost);
            } else
                SkillsLang.NOT_ENOUGH_SOULS.sendMessage(player, "%mastery%", mastery.getDisplayName(), "%cost%", cost);
        } else if (lvl > 0) {
            int cost = mastery.getDowngradeCost(info);
            info.addSouls(cost);
            info.addMasteryLevel(mastery, -1);
            SkillsLang.MASTERY_DOWNGRADED.sendMessage(player, "%mastery%", mastery.getDisplayName(), "%cost%", cost);
        } else SkillsLang.MASTERY_CANT_DOWNGRADE.sendMessage(player, "%mastery%", mastery.getDisplayName());
        openMenu(info, player, true);
    }

    public static void openMenu(SkilledPlayer info, Player player, boolean refresh) {
        InteractiveGUI gui = GUIParser.parseOption(player, "masteries");

        for (Mastery mastery : MasteryManager.getMasteries()) {
            String scaling;
            try {
                scaling = String.valueOf(mastery.getScaling(info));
            } catch (ArithmeticException ignored) {
                scaling = mastery.getScalingEquation(info);
            }
            Object[] edits = {"%cost%", mastery.getUpgradeCost(info), "%level%", info.getMasteryLevel(mastery), "%max-level%", mastery.getMaxLevel(),
                    "%required-level%", mastery.getRequiredLevel(info), "%amount%", scaling};

            InteractiveGUI.ActionRunnable[] actions;
            if (SkillsMasteryConfig.DOWNGRADE.getBoolean())
                actions = new InteractiveGUI.ActionRunnable[]{new InteractiveGUI.ActionRunnable(ClickType.LEFT, () -> setMastery(info, player, mastery, true)),
                        new InteractiveGUI.ActionRunnable(ClickType.RIGHT, () -> setMastery(info, player, mastery, false))};
            else
                actions = new InteractiveGUI.ActionRunnable[]{new InteractiveGUI.ActionRunnable(ClickType.LEFT, () -> setMastery(info, player, mastery, true))};

            gui.push(mastery.getConfigName(), edits, null, actions);
        }

        gui.setRest();
        gui.openInventory(player, refresh);
    }
}
