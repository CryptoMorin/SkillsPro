package org.skills.utils.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class OldInventoryView implements BukkitInventoryView {
    private final InventoryView view;

    public OldInventoryView(InventoryView view) {this.view = view;}

    @Override
    public Inventory getTopInventory() {
        return view.getTopInventory();
    }

    @Override
    public Inventory getBottomInventory() {
        return view.getBottomInventory();
    }

    @Override
    public HumanEntity getPlayer() {
        return view.getPlayer();
    }

    @Override
    public InventoryType getType() {
        return view.getType();
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        view.setItem(slot, item);
    }

    @Override
    public ItemStack getItem(int slot) {
        return view.getItem(slot);
    }

    @Override
    public void setCursor(ItemStack item) {
        view.setCursor(item);
    }

    @Override
    public ItemStack getCursor() {
        return view.getCursor();
    }

    @Override
    public int convertSlot(int slot) {
        return view.convertSlot(slot);
    }

    @Override
    public void close() {
        view.close();
    }

    @Override
    public int countSlots() {
        return view.countSlots();
    }

    @Override
    public String getTitle() {
        return view.getTitle();
    }
}
