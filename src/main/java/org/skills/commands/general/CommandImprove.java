package org.skills.commands.general;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
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
import org.skills.abilities.KeyBinding;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIOption;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandImprove extends SkillsCommand {
    public CommandImprove() {
        super("improve", SkillsLang.COMMAND_IMPROVE_DESCRIPTION, "improvement", "improvements", "upgrade", "upgrades", "ability", "abilities");
    }

    @SuppressWarnings({"UseBulkOperation", "ManualArrayToCollectionCopy"})
    public static void openMenu(Player player, SkilledPlayer info, int page, boolean refresh) {
        Collection<Ability> abilities = info.getSkill().getAbilities();
        int size = abilities.size() - 1; // -1 for the passive ability
        int pages = size / 4;
        if (size % 4 != 0) pages++;

        // Pages
        if (size > 4) {
            abilities = abilities.stream()
                    .filter(x -> !x.getName().endsWith("passive"))
                    .sorted(Comparator.comparingInt(x -> x.getRequiredLevel(info)))
                    .skip(page * 4L).limit(4)
                    .collect(Collectors.toList());
            abilities.add(info.getSkill().getAbilities().stream().filter(x -> x.getName().endsWith("passive")).findFirst()
                    .orElseThrow(() -> new IllegalStateException(info.getSkillName() + " skill has no passive ability.")));
        } else pages = 1;

        int finalPages = pages;
        InteractiveGUI gui = GUIParser.parseOption(player, "abilities",
                "%page%", page + 1, "%pages%", pages);

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
        int passiveSlot = -1;

        for (Ability ability : abilities) {
            boolean isSuper = ability.getName().endsWith("passive");
            boolean isPassive = ability.isPassive();
            int lvl = info.getAbilityLevel(ability);
            ActiveAbility activeAbs = isPassive ? null : (ActiveAbility) ability;
            KeyBinding[] binding = info.getAbilityData(ability).getKeyBinding();

            Object[] manualEdits = {
                    "%required-level%", ability.getRequiredLevel(info),
                    "%title%", ability.getTitle(info),
                    "%level%", lvl,
                    "%disabled%", info.isAbilityDisabled(ability),
                    "%cost%", ability.getCost(info),
                    "%activation_cooldown%", activeAbs == null ? "" : activeAbs.getCooldown(info),
                    "%activation_energy%", activeAbs == null ? "" : activeAbs.getEnergy(info),
                    "%activation_key%", KeyBinding.toString(binding == null ?
                    (activeAbs == null ? new KeyBinding[0] : activeAbs.getActivationKey(info)) : binding),
            };
            if (slots == null) {
                slots = new ArrayList<>(gui.getHolder("ability", manualEdits).getSlots());
                passiveSlot = gui.getHolder("passive", manualEdits).getSlots().get(0);
            }

            String optionName;
            if (isPassive) {
                if (isSuper) optionName = "passive";
                else optionName = "ability";
            } else optionName = "active-ability";
            GUIOption option = gui.getHolder(optionName, manualEdits);
            ItemStack clone = option.getItem();
            ItemMeta meta = option.getItem().getItemMeta();

            List<String> translatedLore = new ArrayList<>();
            for (String lore : meta.getLore()) {
                if (!Strings.isNullOrEmpty(lore)) {
                    lore = MessageHandler.replace(lore, "%description%", (Supplier<String>) () -> ability.getDescription(info));
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
            List<Object> edits = ability.getEdits(info);
            for (Object edit : manualEdits) edits.add(edit);
            Object[] arrayEdits = edits.toArray();
            option.defineVariables(gui, edits);

            int slot = isSuper ? passiveSlot : slots.remove(0);
            gui.push(option, clone, slot, arrayEdits, null, new InteractiveGUI.ActionRunnable(ClickType.LEFT, () -> {
                if (isSuper || info.getAbilityLevel(ability) >= 3) {
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
                    info.addAbilityLevel(ability, 1);
                    info.setSouls(souls - cost);

                    SkillsLang.ABILITY_UPGRADED.sendMessage(player);
                    XSound.BLOCK_ANVIL_USE.play(player);
                    openMenu(player, info, page, true);
                } else {
                    SkillsLang.NOT_ENOUGH_SOULS.sendMessage(player);
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                }
            }), new InteractiveGUI.ActionRunnable(ClickType.RIGHT, () -> {
                if (info.getAbilityLevel(ability) == 0) {
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
