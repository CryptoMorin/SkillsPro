package org.skills.gui;

import com.cryptomorin.xseries.XSound;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.kingdoms.server.inventory.BukkitInventoryView;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.StringUtils;
import org.skills.utils.XInventoryView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class InteractiveGUIManager implements Listener {
    protected static final Map<Integer, GUIOption> CONVERSATION = new HashMap<>();
    protected static final Map<Integer, InteractiveGUI> GUIS = new HashMap<>();

    private static int firstPartial(Inventory inventory, ItemStack item, Predicate<Integer> slot) {
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slot.test(i)) {
                ItemStack compare = items[i];
                if (compare != null && compare.isSimilar(item) && compare.getMaxStackSize() != compare.getAmount())
                    return i;
            }
        }
        return -1;
    }

    public static Map<Integer, InteractiveGUI> getGuis() {
        return GUIS;
    }

    private static List<ItemStack> distribute(Inventory inventory, ItemStack item, Predicate<Integer> slot) {
        int maxStack = item.getMaxStackSize();
        int partialSlot;
        while ((partialSlot = firstPartial(inventory, item, slot)) != -1) {
            ItemStack partial = inventory.getItem(partialSlot);
            if (item.getAmount() + partial.getAmount() <= maxStack) {
                partial.setAmount(partial.getAmount() + item.getAmount());
                return new ArrayList<>();
            } else {
                int amount = maxStack - partial.getAmount();
                partial.setAmount(partial.getAmount() + amount);
                item.setAmount(item.getAmount() - amount);
            }
        }

        List<ItemStack> distributed = new ArrayList<>();
        while (item.getAmount() > maxStack) {
            ItemStack clone = item.clone();

            clone.setAmount(maxStack);
            distributed.add(clone);
            item.setAmount(item.getAmount() - maxStack);
        }
        distributed.add(item);

        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slot.test(i) && items[i] == null) {
                inventory.setItem(i, distributed.remove(0));
                if (distributed.isEmpty()) return distributed;
            }
        }

        return distributed;
    }

    private static void end(Player player) {
        CONVERSATION.remove(player.getEntityId());
        InteractiveGUI gui = GUIS.remove(player.getEntityId());
        if (gui != null) gui.cancelRefreshTask();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GUIOption option = CONVERSATION.get(player.getEntityId());
        if (option == null) return;
        event.setCancelled(true);

        InteractiveGUI gui = GUIS.get(player.getEntityId());
        String msg = event.getMessage();
        Runnable runnable;

        if (msg.equalsIgnoreCase("cancel")) {
            InteractiveGUI.endConversation(player);
            runnable = () -> gui.openInventory(player);
        } else {
            runnable = () -> option.getConversation().run(msg);
        }
        Bukkit.getScheduler().runTask(SkillsPro.get(), runnable);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        InteractiveGUI gui = GUIS.get(player.getEntityId());
        if (gui == null) return;
        event.setCancelled(true);
        if (true) return;

        BukkitInventoryView view = XInventoryView.of(event.getView());
        boolean conflict = false;
        for (Map.Entry<Integer, ItemStack> item : event.getNewItems().entrySet()) {
            int rawSlot = item.getKey();

            Inventory top = view.getTopInventory();
            if (rawSlot < top.getSize()) {
                int slot = view.convertSlot(rawSlot);
                if (!gui.isSlotInteractable(slot)) {
                    conflict = true;
                    break;
                }
            }
        }

        if (conflict) {
            int cantSet = 0;
            event.setCancelled(true);

            for (Map.Entry<Integer, ItemStack> item : event.getNewItems().entrySet()) {
                int rawSlot = item.getKey();

                Inventory top = view.getTopInventory();
                if (rawSlot < top.getSize()) {
                    int slot = view.convertSlot(rawSlot);
                    if (!gui.isSlotInteractable(slot)) {
                        cantSet += item.getValue().getAmount();
                        continue;
                    }
                }

                view.setItem(rawSlot, item.getValue());
            }

            if (cantSet != 0) {
                ItemStack cursor;
                if (event.getCursor() == null) {
                    cursor = event.getOldCursor(); // .clone();
                    cursor.setAmount(cantSet);
                } else {
                    cursor = event.getCursor();
                    cursor.setAmount(cursor.getAmount() + cantSet);
                }
                Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> view.setCursor(cursor), 1);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.NOTHING) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;

        Player player = (Player) event.getWhoClicked();
        InteractiveGUI gui = GUIS.get(player.getEntityId());
        if (gui == null) return;

        if (inventory.getType() == InventoryType.PLAYER) {
            if (event.getCurrentItem() != null && event.isShiftClick()) {
                if (gui.getInteractableSlots().isEmpty()) {
                    event.setCancelled(true);
                    return;
                }

                if (gui.canInteractWithEmpty()) return;
                List<ItemStack> left = distribute(player.getOpenInventory().getTopInventory(), event.getCurrentItem(), gui::isSlotInteractable);
                if (left.isEmpty()) event.setCurrentItem(null);
                event.setCancelled(true);
            }
            return;
        }

        if (gui.getInteractableSlots().contains(event.getSlot())) return;
        GUIOption option = gui.getAction(event.getSlot());
        if (option == null) {
            if (!gui.canInteractWithEmpty()) event.setCancelled(true);
            return;
        }

        if (!option.canBeTaken() || event.getCursor() != null) event.setCancelled(true);
        if (option.getRunnables() != null) {
            Map<ClickType, Runnable> options = option.getRunnables();
            Runnable runnable = options.size() == 1 ? options.values().iterator().next() : options.get(event.getClick());
            if (runnable != null) {
                try {
                    runnable.run();
                } catch (Exception ex) {
                    MessageHandler.sendConsolePluginMessage("&cAn exception has occurred while handling GUI option '" + option.getName() + "' for player " + player.getName() + ':');
                    ex.printStackTrace();
                }
            }
        }

        StringUtils.performCommands(player, option.getCommands());

        String sound = option.getSound();
        if (sound != null && !sound.equals("default")) XSound.play(sound, x -> x.forPlayers(player));
        if (!Strings.isNullOrEmpty(option.getMessage())) MessageHandler.sendPlayerMessage(player, option.getMessage());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        int id = event.getPlayer().getEntityId();
        if (CONVERSATION.containsKey(id)) return;

        InteractiveGUI gui = GUIS.remove(id);
        if (gui != null) {
            gui.cancelRefreshTask();
            if (gui.getOnClose() != null) gui.getOnClose().run();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        end(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        end(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!SkillsConfig.CLOSE_GUI_ON_DAMAGE.getBoolean()) return;

        Player player = (Player) event.getEntity();
        if (GUIS.containsKey(player.getEntityId())) player.closeInventory();
    }
}
