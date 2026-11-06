package org.skills.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skills.main.SkillsPro;

public final class SkillsItemTag {
    private static final String KEY = "skill_item";

    private SkillsItemTag() {
    }

    private static NamespacedKey getKey() {
        return new NamespacedKey(SkillsPro.get(), KEY);
    }

    public static String get(ItemStack item) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(getKey(), PersistentDataType.STRING);
    }

    public static ItemStack set(ItemStack item, String value) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = getKey();

        if (value == null) {
            container.remove(key);
        } else {
            container.set(key, PersistentDataType.STRING, value);
        }

        item.setItemMeta(meta);
        return item;
    }
}