package org.skills.gui;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.BooleanEval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GUIOption {
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("perm:((?:\\w|\\.|,)+)");

    private final String name;
    private final List<Integer> slots;
    private final String sound;
    private final List<String> commands;
    private final boolean canBeTaken;
    private final ItemStack item;
    private String message;
    private Map<ClickType, Runnable> runnables;
    private Conversable conversation;

    public GUIOption(String name, ItemStack item, List<Integer> slots, String sound, boolean canBeTaken, List<String> commands, String message) {
        this.name = name;
        this.item = item;
        this.slots = slots;
        this.canBeTaken = canBeTaken;
        this.commands = commands;
        this.sound = sound;
        this.message = message;
    }

    public static void defineVariables(ItemStack item, OfflinePlayer placeholder, Object... edits) {
        defineVariables(item, placeholder, Arrays.asList(edits));
    }

    public static void defineVariables(ItemStack item, OfflinePlayer placeholder, List<Object> edits) {
        ItemMeta meta = item.getItemMeta();

        if (meta.hasDisplayName())
            meta.setDisplayName(MessageHandler.colorize(ServiceHandler.translatePlaceholders(placeholder, MessageHandler.replaceVariables(meta.getDisplayName(), edits))));
        if (meta.hasLore()) {
            ArrayList<String> translatedLores = new ArrayList<>();
            for (String lore : meta.getLore()) {
                translatedLores.add(MessageHandler.colorize(ServiceHandler.translatePlaceholders(placeholder, MessageHandler.replaceVariables(lore, edits))));
            }
            meta.setLore(translatedLores);
        }

        item.setItemMeta(meta);
    }

    public static boolean conditional(InteractiveGUI gui, String name, OfflinePlayer player, String condition, List<Object> edits) {
        Validate.notEmpty(condition, "Cannot evaluate null or empty condition.");
        try {
            return BooleanEval.evaluate(condition, expression -> {
                expression = ServiceHandler.translatePlaceholders(player, MessageHandler.replaceVariables(expression, edits));
                if (player instanceof Player) return PERMISSION_PATTERN.matcher(expression).replaceAll(Boolean.toString(((Player) player).hasPermission("$1".replace(',', '.'))));
                else return PERMISSION_PATTERN.matcher(expression).replaceAll("true");
            });
        } catch (IllegalArgumentException ex) {
            MessageHandler.sendConsolePluginMessage("&4Error while evaluating a conditional item &e" + name + " &c in GUI&8: &e" + gui.getName());
            return true;
        }
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public GUIOption clone() {
        GUIOption option = new GUIOption(this.name, item.clone(), new ArrayList<>(slots), sound, canBeTaken, new ArrayList<>(commands), message);
        option.setConversation(conversation);
        option.setRunnables(runnables);
        return option;
    }

    public void defineVariables(InteractiveGUI inv, List<Object> edits) {
        defineVariables(this.item, inv.getPlaceholder(), edits);
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public String getSound() {
        return sound;
    }

    public List<String> getCommands() {
        return commands;
    }

    public boolean canBeTaken() {
        return canBeTaken;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<ClickType, Runnable> getRunnables() {
        return runnables;
    }

    public void setRunnables(Map<ClickType, Runnable> runnables) {
        this.runnables = runnables;
    }

    public Conversable getConversation() {
        return conversation;
    }

    public void setConversation(Conversable conversation) {
        this.conversation = conversation;
    }
}
