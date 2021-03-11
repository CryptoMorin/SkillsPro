package org.skills.commands.general;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;
import org.skills.abilities.ActiveAbility;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIOption;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CommandImprove extends SkillsCommand {
    public CommandImprove() {
        super("improve", SkillsLang.COMMAND_IMPROVE_DESCRIPTION, "improvement", "improvements", "upgrade", "upgrades", "ability", "abilities");
    }

    public static void openMenu(Player player, SkilledPlayer info, int page, boolean refresh) {
        Collection<Ability> abilities = info.getSkill().getAbilities();
        int size = abilities.size();
        int pages = size % 4 != 0 ? (size / 4) + 1 : size / 4;

        // Pages
        if (size > 5) {
            abilities = abilities.stream().filter(x -> !x.getName().endsWith("passive")).skip(page * 4).limit(4).collect(Collectors.toList());
            abilities.add(info.getSkill().getAbilities().stream().filter(x -> x.getName().endsWith("passive")).findFirst().orElse(null));
        } else pages = 1;

        int finalPages = pages;
        Object[] masterEdits = {"%page%", page + 1, "%pages%", pages};

        InteractiveGUI gui = GUIParser.parseOption(player, "abilities", masterEdits);

        gui.push("previous-page", () -> {
            if (page == 0) {
                XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                return;
            }
            openMenu(player, info, page - 1, true);
            XSound.ITEM_BOOK_PAGE_TURN.play(player);
        });
        gui.push("next-page", () -> {
            if (page + 2 > finalPages) {
                XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                return;
            }
            openMenu(player, info, page + 1, true);
            XSound.ITEM_BOOK_PAGE_TURN.play(player);
        });

        ArrayList<Integer> slots = null;
        for (Ability ability : abilities) {
            boolean isSuper = ability.getName().endsWith("passive");
            boolean isPassive = ability.isPassive();
            int lvl = info.getImprovementLevel(ability);

            String scaling = ability.getExtra(info).getString("scaling");
            if (scaling != null) {
                try {
                    scaling = String.valueOf(MathUtils.roundToDigits(ability.getScaling(info), 2));
                } catch (ArithmeticException ex) {
                    scaling = ability.getTranslatedScaling(info, ability.getScalingEquation(info, "scaling"));
                }
                scaling = Ability.getScalingColor(ability.getExtra(info.getSkillName(), "scaling").getString()) + scaling;
            }
            if (scaling == null) scaling = "";

            ActiveAbility activeAbility = isPassive ? null : (ActiveAbility) ability;
            Object[] edits = {"%title%", ability.getTitle(info),
                    "%required-level%", ability.getRequiredLevel(info),
                    "%level%", lvl, "%cost%", isSuper ? "" : ability.getCost(info),
                    "%activation%", isPassive ? "" : activeAbility.getActivationKey(info),
                    "%cooldown%", isPassive ? 0 : activeAbility.getCooldown(info),
                    "%energy%", isPassive ? 0 : activeAbility.getEnergy(info),
                    "%disabled%", info.isAbilityDisabled(ability),
                    "%amount%", scaling};

            GUIOption pass = gui.getHolder("passive", edits);
            GUIOption abs = gui.getHolder("ability", edits);
            int passiveSlot = pass.getSlots().get(0);
            if (slots == null) slots = new ArrayList<>(abs.getSlots());
            GUIOption option = isPassive ? isSuper ? pass : abs : gui.getHolder("active-ability", edits);
            option = option.clone();

            ItemStack clone = option.getItem();
            ItemMeta meta = option.getItem().getItemMeta();

            List<String> translatedLore = new ArrayList<>();
            for (String lore : meta.getLore()) {
                if (!Strings.isNullOrEmpty(lore)) {
                    lore = MessageHandler.replace(lore, "%description%", ability.getDescription(info));
                    String lastColors = "";

                    for (String singleLore : StringUtils.splitPreserveAllTokens(lore, '\n')) {
                        if (singleLore.isEmpty()) {
                            translatedLore.add(" ");
                            continue;
                        }
                        singleLore = lastColors + MessageHandler.colorize(singleLore);
                        translatedLore.add(singleLore);

                        lastColors = ChatColor.getLastColors(singleLore);
                    }
                }
            }
            meta.setLore(translatedLore);
            option.getItem().setItemMeta(meta);

            edits = ability.copyEdits(info, edits);
            option.defineVariables(gui, Arrays.asList(edits));

            int slot = isSuper ? passiveSlot : slots.remove(0);
            gui.push(option, clone, slot, edits, null, new InteractiveGUI.ActionRunnable(ClickType.LEFT, () -> {
                if (isSuper || info.getImprovementLevel(ability) >= 3) {
                    info.toggleAbility(ability);
                    openMenu(player, info, page, true);
                    XSound.BLOCK_END_PORTAL_FRAME_FILL.play(player);
                    return;
                }

                int required = ability.getRequiredLevel(info);
                if (required > info.getLevel()) {
                    SkillsLang.ABILITY_REQUIRED_LEVEL.sendMessage(player, "%level%", required);
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                    return;
                }

                int cost = ability.getCost(info);
                long souls = info.getSouls();

                if (souls >= cost) {
                    info.addImprovementLevel(ability, 1);
                    info.setSouls(souls - cost);

                    SkillsLang.ABILITY_UPGRADED.sendMessage(player);
                    XSound.BLOCK_ANVIL_USE.play(player);
                    openMenu(player, info, page, true);
                } else {
                    SkillsLang.NOT_ENOUGH_SOULS.sendMessage(player);
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                }
            }), new InteractiveGUI.ActionRunnable(ClickType.RIGHT, () -> {
                if (info.getImprovementLevel(ability) == 0) {
                    int required = ability.getRequiredLevel(info);
                    if (required > info.getLevel()) {
                        SkillsLang.ABILITY_REQUIRED_LEVEL.sendMessage(player, "%level%", required);
                        XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                        return;
                    }
                }

                info.toggleAbility(ability);
                XSound.BLOCK_END_PORTAL_FRAME_FILL.play(player);
                openMenu(player, info, page, true);
            }));

            if (!isSuper) {
                int[] levelBars = {slot + 1, slot + 2, slot + 3};

                for (int levelBar : levelBars) {
                    if (lvl == 3) {
                        gui.getInventory().setItem(levelBar,
                                getDecal(XMaterial.GREEN_STAINED_GLASS_PANE));
                    } else if (lvl > 0) {
                        lvl--;
                        gui.getInventory().setItem(levelBar,
                                getDecal(XMaterial.ORANGE_STAINED_GLASS_PANE));
                    } else {
                        gui.getInventory().setItem(levelBar,
                                getDecal(XMaterial.BLACK_STAINED_GLASS_PANE));
                    }
                }

                if (lvl == 3) {
                    gui.getInventory().setItem(slot + 4,
                            getDecal(XMaterial.BLUE_STAINED_GLASS_PANE));
                } else {
                    gui.getInventory().setItem(slot + 4,
                            getDecal(XMaterial.GRAY_STAINED_GLASS_PANE));
                }
            }
        }

        gui.dispose("ability", "active-ability", "passive");
        gui.setRest();
        gui.openInventory(player, refresh);
    }

    private static ItemStack getDecal(XMaterial mat) {
        ItemStack decal = mat.parseItem();
        ItemMeta meta = decal.getItemMeta();
        meta.setDisplayName(" ");
        decal.setItemMeta(meta);
        return decal;
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(p);
            if (!info.hasSkill()) {
                SkillsLang.NO_SKILL.sendMessage(p);
                return;
            }
            openMenu(p, info, 0, false);
        } else {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
