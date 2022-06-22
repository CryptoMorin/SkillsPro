package org.skills.gui;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XSound;
import com.google.common.base.Strings;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class GUIParser {
    public static ItemStack deserializeItem(@NotNull InteractiveGUI gui, ConfigurationSection section) {
        ItemStack item = XItemStack.deserialize(section, new ItemColorHandler());
        if (item.getType() == Material.AIR) {
            MessageHandler.sendConsolePluginMessage("&4Could not parse item for option: &e" + section.getName() + " &4in GUI &e" + gui.getName() + " &4with properties&8:");
            section.getValues(true).forEach((k, v) -> MessageHandler.sendConsolePluginMessage("&6" + k + "&8: &e" + (v instanceof ConfigurationSection ? "" : v)));
            return null;
        }
        return item;
    }

    private static final class ItemColorHandler implements Function<String, String> {
        private String lastColor;

        @Override
        public String apply(String s) {
            if (lastColor != null) s = MessageHandler.colorize(lastColor + s);
            lastColor = ChatColor.getLastColors(s);
            return s;
        }
    }

    private static String translate(OfflinePlayer player, String message, List<Object> edits) {
        return MessageHandler.replaceVariables(ServiceHandler.translatePlaceholders(player, message), edits);
    }

    public static InteractiveGUI parseOption(Player player, String name) {
        return parseOption(player, player, name, new ArrayList<>());
    }

    public static InteractiveGUI parseOption(Player player, String name, Object... edits) {
        return parseOption(player, player, name, edits);
    }

    public static InteractiveGUI parseOption(Player player, OfflinePlayer placeholder, String name, Object... edits) {
        return parseOption(player, placeholder, name, Arrays.asList(edits));
    }

    public static InteractiveGUI parseOption(Player player, OfflinePlayer placeholder, String name, List<Object> edits) {
        Configuration config = GUIConfig.getGUI(name);
        if (config == null) return null;
        config.options().pathSeparator('\u0000'); // NULL
        boolean disallowCreative = config.getBoolean("disallow-creative");
        if (disallowCreative && player.getGameMode() == GameMode.CREATIVE) {
            MessageHandler.sendPlayerMessage(player, "&cYou can't open this GUI in creative mode.");
            XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
            return null;
        }

        String title = MessageHandler.replaceVariables(config.getString("title"), edits);
        String type = config.getString("type");
        String sound = config.getString("sound");
        List<String> openCommands = config.getStringList("commands");

        List<Integer> interactable = config.getIntegerList("interactable");
        if (!interactable.isEmpty()) {
            int first = interactable.get(0);
            if (first < 0) {
                interactable.set(0, -interactable.get(0));
                List<Integer> exludeInteractable = new ArrayList<>();
                for (int i = 0; i < 53; i++) {
                    if (!interactable.contains(i)) exludeInteractable.add(i);
                }

                interactable = exludeInteractable;
            }
        }

        int slot = -1;
        InventoryType invType = null;
        if (type == null) {
            slot = config.getInt("rows") * 9;
            if (slot < 1) {
                MessageHandler.sendConsolePluginMessage("&4Invalid rows for slots '&e" + slot + "&4' for GUI&8: &e" + name);
                return null;
            }
        } else {
            try {
                type = type.toUpperCase(Locale.ENGLISH);
                invType = InventoryType.valueOf(type);
            } catch (IllegalArgumentException ex) {
                MessageHandler.sendConsolePluginMessage("&4Could not find inventory type '&e" + type + "&4' for GUI&8: &e" + name);
                return null;
            }
        }

        if (title != null) title = MessageHandler.colorize(ServiceHandler.translatePlaceholders(player, title));
        String guiMessage = config.getString("message");
        if (!Strings.isNullOrEmpty(guiMessage)) guiMessage = translate(placeholder, guiMessage, edits);
        ConfigurationSection options = config.getConfigurationSection("options");
        return type == null ?
                new InteractiveGUI(player, placeholder, name, title, slot, sound, openCommands, interactable, guiMessage, disallowCreative, options, edits) :
                new InteractiveGUI(player, placeholder, invType, name, title, sound, openCommands, interactable, guiMessage, disallowCreative, options, edits);
    }

    public static GUIOption option(InteractiveGUI gui, String option, OfflinePlayer placeholder, List<Object> edits) {
        ConfigurationSection itemConfig = gui.getOptionsSection().getConfigurationSection(option);
        if (itemConfig == null) return null;

        if (itemConfig.getString("material") == null) {
            for (String condition : itemConfig.getKeys(false)) {
                ConfigurationSection masterSection = itemConfig.getConfigurationSection(condition);
                if (masterSection == null) continue;
                String masterCondition = masterSection.getString("condition");
                if (GUIOption.conditional(gui, option, placeholder, masterCondition, edits)) {
                    itemConfig = masterSection;
                    break;
                }
            }
        }

        ItemStack item = deserializeItem(gui, itemConfig);
        if (item == null) return null;
        if (item.getAmount() > gui.getInventory().getMaxStackSize()) gui.getInventory().setMaxStackSize(item.getAmount());

        String itemSound = itemConfig.getString("sound");
        ConfigurationSection optSect = itemConfig.getConfigurationSection("sound");
        if (optSect != null) {
            for (String condition : optSect.getKeys(false)) {
                if (GUIOption.conditional(gui, option, placeholder, condition, edits)) {
                    itemSound = optSect.getString(condition);
                    break;
                }
            }
        }

        List<String> commands = itemConfig.getStringList("commands");
        boolean canBeTaken = itemConfig.getBoolean("can-take");
        boolean refresh = itemConfig.getBoolean("refresh");

        String message = itemConfig.getString("message");
        optSect = itemConfig.getConfigurationSection("message");
        if (optSect != null) {
            for (String condition : optSect.getKeys(false)) {
                if (GUIOption.conditional(gui, option, placeholder, condition, edits)) {
                    message = optSect.getString(condition);
                    break;
                }
            }
        }
        if (!Strings.isNullOrEmpty(message)) message = ServiceHandler.translatePlaceholders(placeholder, message);

        List<Integer> slots = new ArrayList<>(itemConfig.getIntegerList("slots"));
        optSect = itemConfig.getConfigurationSection("slots");
        if (optSect != null) {
            for (String condition : optSect.getKeys(false)) {
                if (GUIOption.conditional(gui, option, placeholder, condition, edits)) {
                    slots = optSect.getIntegerList(condition);
                    break;
                }
            }
        }

        if (slots.isEmpty()) {
            itemConfig = gui.getOptionsSection().getConfigurationSection(option);
            int itemSlot = itemConfig.getInt("slot");
            if (!itemConfig.contains("slot")) {
                int posX, posY;
                if (itemConfig.contains("posx") && itemConfig.contains("posy")) {
                    posX = itemConfig.getInt("posx");
                    posY = itemConfig.getInt("posy");
                } else {
                    MessageHandler.sendConsolePluginMessage("&4Could not find item slot option for item&8: &e" + option);
                    return new GUIOption(option, item, new ArrayList<>(), itemSound, canBeTaken, commands, message);
                }

                itemSlot = (posY * 9 - (9 - posX)) - 1;
            }

            slots.add(itemSlot);
        }

        if (refresh) gui.getRefresh().addAll(slots);
        return new GUIOption(option, item, slots, itemSound, canBeTaken, commands, message);
    }
}
