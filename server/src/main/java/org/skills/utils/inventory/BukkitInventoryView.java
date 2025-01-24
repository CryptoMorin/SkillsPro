package org.skills.utils.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface BukkitInventoryView {
   Inventory getTopInventory();

   Inventory getBottomInventory();

   HumanEntity getPlayer();

   InventoryType getType();

   void setItem(int slot, ItemStack item);

   ItemStack getItem(int slot);

   void setCursor(ItemStack item);

   ItemStack getCursor();

   int convertSlot(int slot);

   void close();

   int countSlots();

   String getTitle();
}