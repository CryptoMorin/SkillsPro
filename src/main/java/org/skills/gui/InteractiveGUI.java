package org.skills.gui;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XSound;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InteractiveGUI {
    private final String name;
    private final Set<String> holders;
    private final Map<Integer, GUIOption> options = new ConcurrentHashMap<>();
    private final ConfigurationSection optionsSection;
    private final List<Integer> refresh = new ArrayList<>();
    private final List<Integer> interactableSlots;
    private final List<String> commands;
    private final String sound;
    private final Inventory inventory;
    private final boolean disallowCreative;
    private final List<Object> edits;
    private final Player owner;
    private final OfflinePlayer placeholder;
    private final String message;
    private Integer refreshTask;
    private Runnable onClose;

    public InteractiveGUI(Inventory inventory, Player owner, OfflinePlayer placeHolder, String name, String sound, List<String> commands,
                          List<Integer> interactableSlots, String message, boolean disallowCreative,
                          ConfigurationSection optionsSection, List<Object> edits) {
        this.inventory = inventory;
        this.name = name;
        this.sound = sound;
        this.commands = commands;
        this.interactableSlots = interactableSlots;
        this.message = message;
        this.disallowCreative = disallowCreative;
        this.optionsSection = optionsSection;
        this.edits = edits;
        this.owner = owner;
        this.placeholder = placeHolder;

        holders = ConcurrentHashMap.newKeySet();
        if (optionsSection != null) holders.addAll(optionsSection.getKeys(false));
    }

    public InteractiveGUI(Player holder, OfflinePlayer placeholder, String name, String title, int size, String sound,
                          List<String> commands, List<Integer> interactableSlots, String message,
                          boolean disallowCreative, ConfigurationSection optionsSection, List<Object> edits) {
        this(Bukkit.createInventory(holder, size, title), holder, placeholder, name, sound, commands, interactableSlots, message, disallowCreative, optionsSection, edits);
    }

    public InteractiveGUI(Player holder, OfflinePlayer placeholder, InventoryType type, String name, String title, String sound, List<String> commands,
                          List<Integer> interactableSlots, String message,
                          boolean disallowCreative, ConfigurationSection optionsSection, List<Object> edits) {
        this(Bukkit.createInventory(holder, type, title), holder, placeholder, name, sound, commands, interactableSlots, message, disallowCreative, optionsSection, edits);
    }

    public static void endConversation(Player player) {
        InteractiveGUIManager.CONVERSATION.remove(player.getEntityId());
    }

    public void startConversation(Player player, String holder) {
        holders.add(holder);
        InteractiveGUIManager.CONVERSATION.put(player.getEntityId(), options.get(getHolder(holder).getSlots().get(0)));
        player.closeInventory();
    }

    public void refresh() {
        for (GUIOption option : options.values()) {
            ItemStack item = GUIParser.deserializeItem(this, optionsSection.getConfigurationSection(option.getName()));
            GUIOption.defineVariables(item, owner, this.edits);
            option.getSlots().forEach(slot -> owner.getOpenInventory().getTopInventory().setItem(slot, item));
        }
        owner.updateInventory();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public InteractiveGUI push(String holder, Runnable runnable, Object... edits) {
        return push(holder, edits, null, new ActionRunnable(ClickType.LEFT, runnable));
    }

    public InteractiveGUI push(String holder, Runnable runnable, Conversable conversation, Object... edits) {
        return push(holder, edits, conversation, new ActionRunnable(ClickType.LEFT, runnable));
    }

    public Set<String> getHolders() {
        return holders;
    }

    public InteractiveGUI push(String holder, Object[] edits, Runnable runnable) {
        return push(holder, edits, null, new ActionRunnable(ClickType.LEFT, runnable));
    }

    public Player getOwner() {
        return this.owner;
    }

    public InteractiveGUI push(String holder, Object[] edits, Conversable conversation, ActionRunnable... runnables) {
        List<Object> editors = wrapEditors(edits);
        GUIOption option = GUIParser.option(this, holder, placeholder, editors);
        if (option == null) return this;

        option.defineVariables(this, editors);
        option.getSlots().forEach(i -> inventory.setItem(i, option.getItem()));

        Map<ClickType, Runnable> runnableMap = new EnumMap<>(ClickType.class);
        for (ActionRunnable runnable : runnables) runnableMap.put(runnable.getClick(), runnable.getRunnable());
        option.setRunnables(runnableMap);
        option.setConversation(conversation);

        holders.remove(holder);
        option.getSlots().forEach(i -> options.put(i, option));
        return this;
    }

    public Map<Integer, GUIOption> getOptions() {
        return options;
    }

    public InteractiveGUI push(GUIOption option, ItemStack item, int slot, Runnable runnable, Object... edits) {
        return push(option, item, slot, edits, null, new ActionRunnable(ClickType.LEFT, runnable));
    }

    private List<Object> wrapEditors(Object[] edits) {
        if (edits == null || edits.length == 0) return this.edits;
        List<Object> editors = new ArrayList<>(Arrays.asList(edits));
        editors.addAll(this.edits);
        return editors;
    }

    public InteractiveGUI push(GUIOption option, ItemStack item, int slot, Object[] edits, Conversable conversation, ActionRunnable... runnables) {
        if (item == null) item = option.getItem();
        GUIOption.defineVariables(item, placeholder, wrapEditors(edits));

        Map<ClickType, Runnable> runnableMap = new EnumMap<>(ClickType.class);
        for (ActionRunnable runnable : runnables) runnableMap.put(runnable.getClick(), runnable.getRunnable());
        option.setRunnables(runnableMap);
        option.setConversation(conversation);

        inventory.setItem(slot, item);
        options.put(slot, option);
        return this;
    }

    public GUIOption getHolder(String holder, Object... edits) {
        return GUIParser.option(this, holder, placeholder, wrapEditors(edits));
    }

    public GUIOption getHoldingOption(String holder, Object... edits) {
        GUIOption option = getHolder(holder, edits);
        if (option == null) return null;

        option.getSlots().forEach(i -> options.put(i, option));
        holders.remove(holder);
        return option;
    }

    public void dispose(String holder, GUIOption option) {
        option.getSlots().forEach(i -> options.put(i, option));
        dispose(holder);
    }

    public void dispose(String... disposables) {
        for (String disposable : disposables) holders.remove(disposable);
    }

    public void setRest() {
        for (String left : holders) {
            GUIOption option = GUIParser.option(this, left, placeholder, edits);
            if (option == null) continue;

            option.defineVariables(this, edits);
            option.getSlots().forEach(i -> {
                inventory.setItem(i, option.getItem());
                options.put(i, option);
            });
        }
        holders.clear();
    }

    public void openInventory(Player player) {
        openInventory(player, false, false);
    }

    public void openInventory(Player player, boolean refresh) {
        openInventory(player, refresh, false);
    }

    public void openInventory(Player player, boolean refresh, boolean update) {
        if (!update) {
            if (Bukkit.isPrimaryThread()) {
                player.openInventory(inventory);
                InteractiveGUI previous = InteractiveGUIManager.GUIS.put(player.getEntityId(), this);
                if (previous != null) previous.cancelRefreshTask();
            } else Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                player.openInventory(inventory);
                InteractiveGUI previous = InteractiveGUIManager.GUIS.put(player.getEntityId(), this);
                if (previous != null) previous.cancelRefreshTask();
            });
        }

        InteractiveGUIManager.CONVERSATION.remove(player.getEntityId());
        if (!refresh) {
            StringUtils.performCommands(player, commands);
            if (!Strings.isNullOrEmpty(sound)) XSound.play(sound, x -> x.forPlayers(player));
            if (!Strings.isNullOrEmpty(message)) MessageHandler.sendPlayerMessage(player, message);
        } else if (update) refresh();
    }

    public GUIOption getAction(int slot) {
        return options.get(slot);
    }

    public void onClose(Runnable run) {
        this.onClose = run;
    }

    public Runnable getOnClose() {
        return onClose;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getInteractableSlots() {
        if (canInteractWithEmpty()) {
            List<Integer> interactable = new ArrayList<>();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (!options.containsKey(slot)) interactable.add(slot);
            }
            return interactable;
        } else {
            return interactableSlots;
        }
    }

    public boolean canInteractWithEmpty() {
        return interactableSlots.size() == 1 && interactableSlots.get(0) == 999;
    }

    public int firstEmpty() {
        for (int slot : interactableSlots) {
            if (inventory.getItem(slot) == null) return slot;
        }
        return -1;
    }

    public boolean isSlotInteractable(int slot) {
        return interactableSlots.contains(slot);
    }

    public List<ItemStack> getInteractableItems() {
        List<ItemStack> items = new ArrayList<>();
        if (canInteractWithEmpty()) {
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (!options.containsKey(slot)) {
                    ItemStack item = inventory.getItem(slot);
                    if (item != null) items.add(item);
                }
            }
        } else {
            for (int slot : interactableSlots) {
                ItemStack item = inventory.getItem(slot);
                if (item != null) items.add(item);
            }
        }
        return items;
    }

    public void returnItems() {
        XItemStack.giveOrDrop(owner, getInteractableItems().toArray(new ItemStack[0]));
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<Integer> getRefresh() {
        return refresh;
    }

    public Integer getRefreshTask() {
        return refreshTask;
    }

    public void setRefreshTask(BukkitTask task) {
        setRefreshTask(task.getTaskId());
    }

    public void setRefreshTask(Integer task) {
        cancelRefreshTask();
        this.refreshTask = task;
    }

    public void cancelRefreshTask() {
        if (this.refreshTask != null) Bukkit.getScheduler().cancelTask(this.refreshTask);
    }

    public boolean isDisallowCreative() {
        return disallowCreative;
    }

    public ConfigurationSection getOptionsSection() {
        return optionsSection;
    }

    public OfflinePlayer getPlaceholder() {
        return placeholder;
    }

    public static class ActionRunnable {
        private final ClickType click;
        private final Runnable runnable;

        public ActionRunnable(ClickType click, Runnable runnable) {
            this.click = click;
            this.runnable = runnable;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public ClickType getClick() {
            return click;
        }
    }
}
