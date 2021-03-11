package org.skills.utils.nbt;

import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;

public class ItemNBT {
    public static final boolean CAN_ACCESS_UNBREAKABLE;
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle SET_TAG;
    private static final MethodHandle GET_TAG;

    static {
        String majorVersion = XMaterial.getMajorVersion(Bukkit.getVersion());
        CAN_ACCESS_UNBREAKABLE = majorVersion.startsWith("1.1") && Integer.parseInt(majorVersion.substring(3, 4)) != 0;

        MethodHandle asNmsCopy = null;
        MethodHandle asBukkitCopy = null;
        MethodHandle setTag = null;
        MethodHandle getTag = null;

        //if (!XMaterial.isNewVersion()) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> crafItemStack = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
        Class<?> nmsItemStack = ReflectionUtils.getNMSClass("ItemStack");
        Class<?> nbtTagCompound = ReflectionUtils.getNMSClass("NBTTagCompound");
        Class<?> nbtBase = ReflectionUtils.getNMSClass("NBTBase");

        try {
            asNmsCopy = lookup.findStatic(crafItemStack, "asNMSCopy", MethodType.methodType(nmsItemStack, ItemStack.class));
            asBukkitCopy = lookup.findStatic(crafItemStack, "asBukkitCopy", MethodType.methodType(ItemStack.class, nmsItemStack));

            setTag = lookup.findVirtual(nmsItemStack, "setTag", MethodType.methodType(void.class, nbtTagCompound));
            getTag = lookup.findVirtual(nmsItemStack, "getTag", MethodType.methodType(nbtTagCompound));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }

        AS_NMS_COPY = asNmsCopy;
        AS_BUKKIT_COPY = asBukkitCopy;
        SET_TAG = setTag;
        GET_TAG = getTag;
    }

    public static ItemStack addSimpleTag(ItemStack item, String tag, String value) {
//        if (XMaterial.isNewVersion()) {
//            ItemMeta meta = item.getItemMeta();
//            NamespacedKey key = new NamespacedKey(PLUGIN, tag);
//
//            if (XMaterial.supports(14)) {
//                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
//            } else {
//                meta.getCustomTagContainer().setCustomTag(key, ItemTagType.STRING, value);
//            }
//            item.setItemMeta(meta);
//
//            return item;
//        }

        NBTWrappers.NBTTagCompound compound = ItemNBT.getTag(item);
        compound.setString(tag, value);
        return ItemNBT.setTag(item, compound);
    }

    private static Object asNMSCopy(ItemStack item) {
        try {
            return AS_NMS_COPY.invoke(item);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    /**
     * Only pass a NMS Itemstack!
     * @param nmsItem The NMS item to convert
     * @return The converted Item
     */
    private static ItemStack asBukkitCopy(Object nmsItem) {
        try {
            return (ItemStack) AS_BUKKIT_COPY.invoke(nmsItem);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    /**
     * Sets the NBT tag of an item
     * @param tag  The new tag
     * @param item The ItemStack
     * @return The modified itemStack
     */
    public static ItemStack setTag(ItemStack item, NBTWrappers.NBTTagCompound tag) {
        Object nbtTag = tag.toNBT();
        Object nmsItem = asNMSCopy(item);

        try {
            SET_TAG.invoke(nmsItem, nbtTag);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return asBukkitCopy(nmsItem);
    }

    /**
     * Gets the NBTTag of an item. In case of any error it returns a blank one.
     * @param item The ItemStack to get the tag for
     * @return The NBTTagCompound of the ItemStack or a new one if it had none or an error occurred
     */
    public static NBTWrappers.NBTTagCompound getTag(ItemStack item) {
        if (XMaterial.isNewVersion()) {
            ItemMeta meta = item.getItemMeta();
            //NamespacedKey key = new NamespacedKey(PLUGIN, "");

            if (XMaterial.supports(14)) {
                //meta.getPersistentDataContainer().get(key, type.getPersistentDataType());
            } else {
                //meta.getCustomTagContainer().getCustomTag(key, type.getItemTagType());
            }
        }

        Object nmsItem = asNMSCopy(item);
        Object tag = null;
        try {
            tag = GET_TAG.invoke(nmsItem);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if (tag == null) return new NBTWrappers.NBTTagCompound();

        NBTWrappers.NBTBase base = NBTWrappers.NBTBase.fromNBT(tag);
        if (base == null || base.getClass() != NBTWrappers.NBTTagCompound.class) return new NBTWrappers.NBTTagCompound();
        return (NBTWrappers.NBTTagCompound) base;
    }

    public static ItemStack setUnbreakable(ItemStack item, boolean unbreakable) {
        if (CAN_ACCESS_UNBREAKABLE) {
            ItemMeta meta = item.getItemMeta();
            meta.setUnbreakable(unbreakable);
            item.setItemMeta(meta);
            return item;
        }

        NBTWrappers.NBTTagCompound tag = getTag(item);
        tag.set("Unbreakable", NBTType.BOOLEAN, unbreakable);
        return setTag(item, tag);
    }

    protected static ItemStack setAttributes(ItemStack item, boolean unbreakable) {
        if (XMaterial.supports(9)) {
            ItemMeta meta = item.getItemMeta();
            meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier("34", 343, AttributeModifier.Operation.ADD_NUMBER));
            item.setItemMeta(meta);
            return item;
        }

        NBTWrappers.NBTTagCompound tag = getTag(item);
        NBTWrappers.NBTTagList modifiers = new NBTWrappers.NBTTagList();
        NBTWrappers.NBTTagCompound attribute = new NBTWrappers.NBTTagCompound();

        // https://minecraft.gamepedia.com/Attribute
        UUID id = UUID.randomUUID();
        attribute.set("AttributeName", NBTType.STRING, "generic.attackSpeed");
        attribute.set("Name", NBTType.STRING, "generic.attackSpeed");
        attribute.set("Amount", NBTType.INTEGER, 34);
        attribute.set("Operation", NBTType.INTEGER, 3);
        attribute.set("UUIDLeast", NBTType.INTEGER, (int) id.getLeastSignificantBits());
        attribute.set("UUIDMost", NBTType.INTEGER, (int) id.getMostSignificantBits());
        attribute.set("Slot", NBTType.STRING, EquipmentSlot.OFF_HAND.name().toLowerCase().replace("_", ""));
        // "mainhand" "offhand"

        modifiers.add(attribute);
        tag.set("AttributeModifiers", modifiers);
        return setTag(item, tag);
    }
}
